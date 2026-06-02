export interface OverloadRecord {
  id: string;
  date: string;
  userId: string;
  value: string;
}

export interface OverloadPage {
  content: OverloadRecord[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface UserOverloadCount {
  userId: string;
  userName: string;
  count: number;
}

export interface TimeSeriesBucket {
  bucketStart: string;
  count: number;
}

export interface OverloadStatistics {
  topUsers: UserOverloadCount[];
  timeSeries: TimeSeriesBucket[];
}

export interface UserSummary {
  id: string;
  name: string;
}
