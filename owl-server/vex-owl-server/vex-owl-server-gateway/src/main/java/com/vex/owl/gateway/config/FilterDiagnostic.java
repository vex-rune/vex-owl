package com.vex.owl.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

/**
 * 过滤器诊断工具
 * 启动时打印所有注册的 GlobalFilter 及其优先级
 */
@Slf4j
@Component
public class FilterDiagnostic implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void run(String... args) {
        log.debug("╔══════════════════════════════════════════════════════════╗");
        log.debug("║           Gateway GlobalFilter 诊断报告                 ║");
        log.debug("╚══════════════════════════════════════════════════════════╝");
        
        // 获取所有 GlobalFilter
        Map<String, GlobalFilter> filters = applicationContext.getBeansOfType(GlobalFilter.class);
        
        if (filters.isEmpty()) {
            log.warn("⚠️  未找到任何 GlobalFilter！");
            return;
        }
        
        log.debug("📊 共发现 {} 个 GlobalFilter:\n", filters.size());
        
        // 按 order 排序
        TreeMap<Integer, String> orderedFilters = new TreeMap<>();
        
        for (Map.Entry<String, GlobalFilter> entry : filters.entrySet()) {
            String beanName = entry.getKey();
            GlobalFilter filter = entry.getValue();
            
            // 获取 order 值
            int order = getOrder(filter);
            
            String info = String.format("%-30s (order: %4d)", beanName, order);
            orderedFilters.put(order, info);
        }
        
        // 打印排序后的过滤器
        int index = 1;
        for (Map.Entry<Integer, String> entry : orderedFilters.entrySet()) {
            log.debug("  {}. {} ← 优先级 {}", index++, entry.getValue(), 
                    entry.getKey() == -200 ? "最高" : 
                    entry.getKey() == -150 ? "高" : 
                    entry.getKey() == -100 ? "中" : "低");
        }
        
        log.debug("\n💡 提示: order 值越小，优先级越高（越先执行）");
        log.debug("═══════════════════════════════════════════════════════════");
    }
    
    /**
     * 获取过滤器的 order 值
     */
    private int getOrder(GlobalFilter filter) {
        if (filter instanceof Ordered ) {
            log.debug("Ordered: name:{}, order:{}", filter.getClass().getName(), ((Ordered) filter).getOrder());
            return ((Ordered) filter).getOrder();
        }
        
        // 尝试从 @Order 注解获取
        Order orderAnnotation = filter.getClass().getAnnotation(Order.class);
        if (orderAnnotation != null) {
            return orderAnnotation.value();
        }
        
        return Integer.MAX_VALUE; // 默认最低优先级
    }
}
