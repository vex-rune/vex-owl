export interface QueriesPageRequest {
  predicate?: Predicate[];
  order?: OrderBy[];
  page?: QueryPage;
}

export interface Predicate {
  field: string;
  op: string;
  value: unknown;
}

export interface OrderBy {
  field: string;
  direction: 'asc' | 'desc';
}

export interface QueryPage {
  page: number;
  size: number;
}