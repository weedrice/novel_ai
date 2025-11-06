export interface Project {
  id: number;
  name: string;
  description?: string;
  owner?: {
    id: number;
    username: string;
    email: string;
  };
  createdAt: string;
  updatedAt: string;
}

export interface CreateProjectRequest {
  name: string;
  description?: string;
}

export interface UpdateProjectRequest {
  name: string;
  description?: string;
}
