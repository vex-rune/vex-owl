class QueriesPageRequest {
  final List<Predicate>? predicate;
  final List<OrderBy>? order;
  final QueryPage? page;

  QueriesPageRequest({
    this.predicate,
    this.order,
    this.page,
  });

  Map<String, dynamic> toJson() {
    final map = <String, dynamic>{};
    if (predicate != null && predicate!.isNotEmpty) {
      map['predicate'] = predicate!.map((p) => p.toJson()).toList();
    }
    if (order != null && order!.isNotEmpty) {
      map['order'] = order!.map((o) => o.toJson()).toList();
    }
    if (page != null) {
      map['page'] = page!.toJson();
    }
    return map;
  }
}

class Predicate {
  final String field;
  final String op;
  final dynamic value;

  Predicate({
    required this.field,
    required this.op,
    required this.value,
  });

  Map<String, dynamic> toJson() => {
        'field': field,
        'op': op,
        'value': value,
      };
}

class OrderBy {
  final String field;
  final String direction;

  OrderBy({
    required this.field,
    this.direction = 'desc',
  });

  Map<String, dynamic> toJson() => {
        'field': field,
        'direction': direction,
      };
}

class QueryPage {
  final int page;
  final int size;

  QueryPage({
    this.page = 0,
    this.size = 20,
  });

  Map<String, dynamic> toJson() => {
        'page': page,
        'size': size,
      };
}