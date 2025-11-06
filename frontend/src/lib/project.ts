import apiClient from './api';
import { Project } from '@/types/project';

export interface ProjectRequest {
  name: string;
  description?: string;
}

/**
 * 내 프로젝트 목록 조회
 */
export const getMyProjects = async (): Promise<Project[]> => {
  const response = await apiClient.get<Project[]>('/projects');
  return response.data;
};

/**
 * 프로젝트 생성
 */
export const createProject = async (data: ProjectRequest): Promise<Project> => {
  const response = await apiClient.post<Project>('/projects', data);
  return response.data;
};

/**
 * 프로젝트 상세 조회
 */
export const getProject = async (id: number): Promise<Project> => {
  const response = await apiClient.get<Project>(`/projects/${id}`);
  return response.data;
};

/**
 * 프로젝트 수정
 */
export const updateProject = async (id: number, data: ProjectRequest): Promise<Project> => {
  const response = await apiClient.put<Project>(`/projects/${id}`, data);
  return response.data;
};

/**
 * 프로젝트 삭제
 */
export const deleteProject = async (id: number): Promise<void> => {
  await apiClient.delete(`/projects/${id}`);
};

/**
 * 프로젝트 검색
 */
export const searchProjects = async (keyword: string): Promise<Project[]> => {
  const response = await apiClient.get<Project[]>('/projects/search', {
    params: { keyword },
  });
  return response.data;
};

/**
 * 현재 선택된 프로젝트 ID 가져오기
 */
export const getCurrentProjectId = (): number | null => {
  if (typeof window === 'undefined') return null;

  const projectId = localStorage.getItem('currentProjectId');
  return projectId ? parseInt(projectId, 10) : null;
};

/**
 * 현재 선택된 프로젝트 ID 저장
 */
export const setCurrentProjectId = (projectId: number) => {
  localStorage.setItem('currentProjectId', projectId.toString());
};

/**
 * 현재 선택된 프로젝트 정보 가져오기
 */
export const getCurrentProject = (): Project | null => {
  if (typeof window === 'undefined') return null;

  const projectStr = localStorage.getItem('currentProject');
  if (!projectStr) return null;

  try {
    return JSON.parse(projectStr) as Project;
  } catch {
    return null;
  }
};

/**
 * 현재 선택된 프로젝트 정보 저장
 */
export const setCurrentProject = (project: Project) => {
  localStorage.setItem('currentProject', JSON.stringify(project));
  localStorage.setItem('currentProjectId', project.id.toString());
};