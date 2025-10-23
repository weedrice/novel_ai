'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Project, getMyProjects, getCurrentProject as getStoredProject, setCurrentProject as setStoredProject } from '@/lib/project';
import { isAuthenticated } from '@/lib/auth';

interface ProjectContextType {
  currentProject: Project | null;
  projects: Project[];
  loading: boolean;
  error: string | null;
  selectProject: (project: Project) => void;
  refreshProjects: () => Promise<void>;
}

const ProjectContext = createContext<ProjectContextType | undefined>(undefined);

export const ProjectProvider = ({ children }: { children: ReactNode }) => {
  const [currentProject, setCurrentProject] = useState<Project | null>(null);
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchProjects = async () => {
    if (!isAuthenticated()) {
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const fetchedProjects = await getMyProjects();
      setProjects(fetchedProjects);

      // 저장된 프로젝트가 있으면 해당 프로젝트를 선택
      const stored = getStoredProject();
      if (stored && fetchedProjects.some(p => p.id === stored.id)) {
        setCurrentProject(stored);
      } else if (fetchedProjects.length > 0) {
        // 없으면 첫 번째 프로젝트 선택
        setCurrentProject(fetchedProjects[0]);
        setStoredProject(fetchedProjects[0]);
      }
    } catch (err: any) {
      console.error('Failed to fetch projects:', err);
      setError('프로젝트 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProjects();
  }, []);

  const selectProject = (project: Project) => {
    setCurrentProject(project);
    setStoredProject(project);
  };

  const refreshProjects = async () => {
    await fetchProjects();
  };

  return (
    <ProjectContext.Provider
      value={{
        currentProject,
        projects,
        loading,
        error,
        selectProject,
        refreshProjects,
      }}
    >
      {children}
    </ProjectContext.Provider>
  );
};

export const useProject = () => {
  const context = useContext(ProjectContext);
  if (context === undefined) {
    throw new Error('useProject must be used within a ProjectProvider');
  }
  return context;
};