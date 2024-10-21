export interface BasicPageType<T>{
  records: T[];
  total: number;
  size: number;
  current: number;
  orders: {column: string; asc: boolean}[];
  optimizeCountSql: boolean;
  searchCount: boolean;
  maxLimit: number;
  countId: string;
  pages: number;
}

export const defaultBasicPage = {
  records: [],
  total: 0,
  size: 0,
  current: 0,
  orders: [],
  optimizeCountSql: false,
  searchCount: false,
  maxLimit: 0,
  countId: '',
  pages: 0
}
