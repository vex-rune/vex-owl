import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/services/template_service.dart';
import '../../../data/models/template_models.dart';
import '../../widgets/common/page_container.dart';

final templateDetailProvider = FutureProvider.family<TemplateEntity?, String>((ref, id) async {
  if (id == 'new') return null;
  try {
    return await ref.read(templateServiceProvider).getTemplate(id);
  } catch (e) {
    return null;
  }
});

final templateFormProvider = StateNotifierProvider<TemplateFormNotifier, TemplateFormState>((ref) {
  return TemplateFormNotifier(ref.read(templateServiceProvider));
});

class TemplateFormState {
  final bool isLoading;
  final bool isSaved;
  final String? error;

  TemplateFormState({
    this.isLoading = false,
    this.isSaved = false,
    this.error,
  });

  TemplateFormState copyWith({bool? isLoading, bool? isSaved, String? error}) {
    return TemplateFormState(
      isLoading: isLoading ?? this.isLoading,
      isSaved: isSaved ?? this.isSaved,
      error: error,
    );
  }
}

class TemplateFormNotifier extends StateNotifier<TemplateFormState> {
  final TemplateService _service;

  TemplateFormNotifier(this._service) : super(TemplateFormState());

  Future<void> updateTemplate(String id, TemplateUpdateRequest request) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      await _service.updateTemplate(id, request);
      state = state.copyWith(isLoading: false, isSaved: true);
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  void reset() {
    state = TemplateFormState();
  }
}

class TemplateEditPage extends ConsumerStatefulWidget {
  final String? templateId;

  const TemplateEditPage({super.key, this.templateId});

  @override
  ConsumerState<TemplateEditPage> createState() => _TemplateEditPageState();
}

class _TemplateEditPageState extends ConsumerState<TemplateEditPage> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _contentController = TextEditingController();
  final _remarkController = TextEditingController();
  bool _enabled = true;
  bool _isNewMode = false;

  @override
  void initState() {
    super.initState();
    _isNewMode = widget.templateId == 'new';
  }

  @override
  void dispose() {
    _nameController.dispose();
    _contentController.dispose();
    _remarkController.dispose();
    super.dispose();
  }

  Future<void> _handleSave() async {
    if (!(_formKey.currentState?.validate() ?? false)) return;

    final request = TemplateUpdateRequest(
      name: _nameController.text.trim(),
      content: _contentController.text,
      remark: _remarkController.text.trim().isEmpty ? null : _remarkController.text.trim(),
      enabled: _enabled,
    );

    if (_isNewMode) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('新建模板功能待实现')),
      );
      return;
    }

    await ref.read(templateFormProvider.notifier).updateTemplate(widget.templateId!, request);
    final formState = ref.read(templateFormProvider);

    if (formState.isSaved && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('保存成功')),
      );
      context.go('/templates');
    } else if (formState.error != null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('保存失败: ${formState.error}')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final templateAsync = _isNewMode
        ? AsyncValue<TemplateEntity?>.data(null)
        : ref.watch(templateDetailProvider(widget.templateId ?? ''));
    final formState = ref.watch(templateFormProvider);

    if (!_isNewMode && templateAsync.hasValue && templateAsync.value != null) {
      final template = templateAsync.value!;
      if (_nameController.text.isEmpty) {
        _nameController.text = template.name;
        _contentController.text = template.content ?? '';
        _remarkController.text = template.remark ?? '';
        _enabled = template.enabled ?? true;
      }
    }

    return PageContainer(
      title: _isNewMode ? '新建模板' : '编辑模板',
      actions: [
        TextButton.icon(
          onPressed: () => context.go('/templates'),
          icon: const Icon(Icons.arrow_back),
          label: const Text('返回'),
        ),
      ],
      child: templateAsync.when(
        data: (_) => _buildForm(formState),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error_outline, size: 48, color: AppColors.error),
              const SizedBox(height: 16),
              Text('加载失败: $error'),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildForm(TemplateFormState formState) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Center(
        child: Container(
          constraints: const BoxConstraints(maxWidth: 600),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(24),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          '模板信息',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        const SizedBox(height: 24),
                        TextFormField(
                          controller: _nameController,
                          decoration: const InputDecoration(
                            labelText: '模板名称',
                            hintText: '请输入模板名称',
                          ),
                          validator: (value) {
                            if (value == null || value.trim().isEmpty) {
                              return '请输入模板名称';
                            }
                            return null;
                          },
                        ),
                        const SizedBox(height: 16),
                        TextFormField(
                          controller: _contentController,
                          decoration: const InputDecoration(
                            labelText: '模板内容',
                            hintText: '请输入模板内容，支持 {{param}} 变量',
                            alignLabelWithHint: true,
                          ),
                          maxLines: 8,
                          validator: (value) {
                            if (value == null || value.trim().isEmpty) {
                              return '请输入模板内容';
                            }
                            return null;
                          },
                        ),
                        const SizedBox(height: 8),
                        Text(
                          '提示: 使用 {{参数名}} 作为变量占位符',
                          style: TextStyle(
                            fontSize: 12,
                            color: AppColors.textTertiary,
                          ),
                        ),
                        const SizedBox(height: 16),
                        TextFormField(
                          controller: _remarkController,
                          decoration: const InputDecoration(
                            labelText: '备注',
                            hintText: '请输入备注信息（可选）',
                          ),
                          maxLines: 3,
                        ),
                        const SizedBox(height: 16),
                        SwitchListTile(
                          title: const Text('启用状态'),
                          subtitle: Text(_enabled ? '模板可正常使用' : '模板已禁用'),
                          value: _enabled,
                          onChanged: (value) {
                            setState(() {
                              _enabled = value;
                            });
                          },
                          contentPadding: EdgeInsets.zero,
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 24),
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    OutlinedButton(
                      onPressed: () => context.go('/templates'),
                      child: const Text('取消'),
                    ),
                    const SizedBox(width: 16),
                    ElevatedButton(
                      onPressed: formState.isLoading ? null : _handleSave,
                      child: formState.isLoading
                          ? const SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            )
                          : const Text('保存'),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}