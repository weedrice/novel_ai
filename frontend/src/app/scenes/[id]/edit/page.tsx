'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Button from '@/components/ui/Button';
import Card from '@/components/Card';
import ErrorMessage from '@/components/ErrorMessage';
import LoadingSpinner from '@/components/LoadingSpinner';
import apiClient from '@/lib/api';

interface Character {
  id: number;
  characterId: string;
  name: string;
  personality: string;
  speakingStyle: string;
}

interface Scene {
  id: number;
  sceneNumber: number;
  location: string;
  mood: string;
  description: string;
  participants: string;
}

interface Dialogue {
  id: number;
  dialogueOrder: number;
  text: string;
  intent: string;
  honorific: string;
  emotion: string;
  character: Character;
}

interface GeneratedDialogue {
  speaker: string;
  characterId: string;
  text: string;
  order: number;
}

export default function SceneEditPage() {
  const params = useParams();
  const router = useRouter();
  const sceneId = params?.id as string;

  const [scene, setScene] = useState<Scene | null>(null);
  const [participants, setParticipants] = useState<Character[]>([]);
  const [dialogues, setDialogues] = useState<Dialogue[]>([]);
  const [generatedDialogues, setGeneratedDialogues] = useState<GeneratedDialogue[]>([]);

  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [provider, setProvider] = useState('openai');
  const [dialogueCount, setDialogueCount] = useState(5);

  // 편집 모달 상태
  const [editingDialogue, setEditingDialogue] = useState<Dialogue | null>(null);
  const [newDialogueText, setNewDialogueText] = useState('');
  const [selectedCharacterId, setSelectedCharacterId] = useState('');

  // 대사 추가 모달 상태
  const [showAddModal, setShowAddModal] = useState(false);
  const [newDialogue, setNewDialogue] = useState({
    characterId: '',
    text: '',
    intent: '',
    honorific: 'banmal',
    emotion: ''
  });

  // 시나리오 버전 관리 상태
  const [versions, setVersions] = useState<any[]>([]);
  const [showVersions, setShowVersions] = useState(false);

  useEffect(() => {
    if (sceneId) {
      fetchSceneData();
      fetchDialogues();
    }
  }, [sceneId]);

  const fetchSceneData = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.get(`/scenes/${sceneId}`);
      const data = response.data;
      setScene(data.scene);
      setParticipants(data.participants || []);
      if (data.participants.length > 0) {
        setSelectedCharacterId(data.participants[0].characterId);
      }
    } catch (err: any) {
      setError(`장면 정보 불러오기 실패: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const fetchDialogues = async () => {
    try {
      const response = await apiClient.get(`/scenes/${sceneId}/dialogues`);
      setDialogues(response.data);
    } catch (err: any) {
      console.error('Failed to fetch dialogues:', err);
    }
  };

  const handleGenerateScenario = async () => {
    setGenerating(true);
    setError(null);
    try {
      const response = await apiClient.post(
        `/scenes/${sceneId}/generate-scenario?provider=${provider}&dialogueCount=${dialogueCount}`
      );
      const data = response.data;

      if (data.generatedDialogues) {
        setGeneratedDialogues(data.generatedDialogues);
      } else if (data.dialogues) {
        setGeneratedDialogues(data.dialogues);
      }
    } catch (err: any) {
      setError(`시나리오 생성 실패: ${err.message}`);
    } finally {
      setGenerating(false);
    }
  };

  const handleExportText = () => {
    if (dialogues.length === 0 && generatedDialogues.length === 0) {
      alert('내보낼 대사가 없습니다.');
      return;
    }

    let content = `# ${scene?.description || '장면'}\n`;
    content += `장소: ${scene?.location || '미정'}\n`;
    content += `분위기: ${scene?.mood || '일반'}\n\n`;
    content += '---\n\n';

    // 기존 대사
    if (dialogues.length > 0) {
      content += '## 기존 대사\n\n';
      dialogues.forEach((d) => {
        content += `${d.character.name}: ${d.text}\n`;
      });
      content += '\n';
    }

    // 생성된 대사
    if (generatedDialogues.length > 0) {
      content += '## 생성된 대사\n\n';
      generatedDialogues.forEach((d) => {
        content += `${d.speaker}: ${d.text}\n`;
      });
    }

    const blob = new Blob([content], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `scene-${sceneId}-scenario.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handleExportJSON = () => {
    if (dialogues.length === 0 && generatedDialogues.length === 0) {
      alert('내보낼 대사가 없습니다.');
      return;
    }

    const data = {
      scene: scene,
      existingDialogues: dialogues,
      generatedDialogues: generatedDialogues,
      exportedAt: new Date().toISOString(),
    };

    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `scene-${sceneId}-scenario.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  // 대사 추가
  const handleAddDialogue = async () => {
    if (!newDialogue.characterId || !newDialogue.text) {
      alert('캐릭터와 대사를 입력해주세요.');
      return;
    }

    try {
      const character = participants.find(p => p.characterId === newDialogue.characterId);
      if (!character) return;

      await apiClient.post('/dialogue', {
        sceneId: Number(sceneId),
        characterId: character.id,
        text: newDialogue.text,
        dialogueOrder: dialogues.length + 1,
        intent: newDialogue.intent,
        honorific: newDialogue.honorific,
        emotion: newDialogue.emotion,
      });

      // 성공 시 대사 목록 새로고침
      await fetchDialogues();
      setShowAddModal(false);
      setNewDialogue({
        characterId: '',
        text: '',
        intent: '',
        honorific: 'banmal',
        emotion: ''
      });
    } catch (err: any) {
      setError(`대사 추가 실패: ${err.message}`);
    }
  };

  // 대사 수정
  const handleUpdateDialogue = async (dialogueId: number, updates: any) => {
    try {
      await apiClient.put(`/dialogue/${dialogueId}`, updates);

      // 성공 시 대사 목록 새로고침
      await fetchDialogues();
      setEditingDialogue(null);
    } catch (err: any) {
      setError(`대사 수정 실패: ${err.message}`);
    }
  };

  // 대사 삭제
  const handleDeleteDialogue = async (dialogueId: number) => {
    if (!confirm('정말 이 대사를 삭제하시겠습니까?')) return;

    try {
      await apiClient.delete(`/dialogue/${dialogueId}`);

      // 성공 시 대사 목록 새로고침
      await fetchDialogues();
    } catch (err: any) {
      setError(`대사 삭제 실패: ${err.message}`);
    }
  };

  // 시나리오 버전 저장
  const handleSaveVersion = async () => {
    const title = prompt('버전 이름을 입력하세요:', `버전 ${new Date().toLocaleString()}`);
    if (!title) return;

    try {
      const content = JSON.stringify({
        dialogues: dialogues,
        generatedDialogues: generatedDialogues,
      });

      await apiClient.post(`/scenes/${sceneId}/scenarios`, { title, content });

      alert('시나리오 버전이 저장되었습니다.');
      await fetchVersions();
    } catch (err: any) {
      setError(`버전 저장 실패: ${err.message}`);
    }
  };

  // 버전 목록 조회
  const fetchVersions = async () => {
    try {
      const response = await apiClient.get(`/scenes/${sceneId}/scenarios`);
      setVersions(response.data);
    } catch (err: any) {
      console.error('Failed to fetch versions:', err);
    }
  };

  // 버전 불러오기
  const handleLoadVersion = async (versionId: number) => {
    try {
      const response = await apiClient.get(`/scenes/scenarios/${versionId}`);
      const version = response.data;

      const content = JSON.parse(version.content);
      if (content.dialogues) setDialogues(content.dialogues);
      if (content.generatedDialogues) setGeneratedDialogues(content.generatedDialogues);

      alert('버전이 불러와졌습니다.');
      setShowVersions(false);
    } catch (err: any) {
      setError(`버전 불러오기 실패: ${err.message}`);
    }
  };

  if (loading) {
    return (
      <main className="min-h-screen bg-gray-50 flex items-center justify-center">
        <LoadingSpinner size="lg" message="장면 정보를 불러오는 중..." />
      </main>
    );
  }

  if (!scene) {
    return (
      <main className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Card>
          <p className="text-gray-600">장면을 찾을 수 없습니다.</p>
          <Button onClick={() => router.push('/scenes')} className="mt-4">
            목록으로 돌아가기
          </Button>
        </Card>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-gray-50 p-6 md:p-10">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="text-3xl md:text-4xl font-bold text-gray-900">
                장면 {scene.sceneNumber} 편집
              </h1>
              <p className="text-gray-600 mt-2">{scene.description}</p>
            </div>
            <button
              onClick={() => router.push('/scenes')}
              className="text-blue-600 hover:text-blue-800 font-medium"
            >
              ← 목록으로
            </button>
          </div>

          <div className="flex flex-wrap gap-4 text-sm text-gray-600">
            <div className="flex items-center gap-2">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
              </svg>
              {scene.location || '장소 미정'}
            </div>
            <div className="flex items-center gap-2">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              {scene.mood || '분위기 미정'}
            </div>
            <div className="flex items-center gap-2">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
              참여 캐릭터: {participants.map(p => p.name).join(', ')}
            </div>
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <ErrorMessage
            message={error}
            onRetry={() => {
              setError(null);
              fetchSceneData();
            }}
            onDismiss={() => setError(null)}
          />
        )}

        {/* Scenario Generation Section */}
        <Card title="시나리오 자동 생성" className="mb-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-700">
                LLM 프로바이더
              </label>
              <select
                value={provider}
                onChange={(e) => setProvider(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="openai">OpenAI GPT</option>
                <option value="claude">Anthropic Claude</option>
                <option value="gemini">Google Gemini</option>
              </select>
            </div>

            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-700">
                생성할 대사 수
              </label>
              <input
                type="number"
                min="3"
                max="20"
                value={dialogueCount}
                onChange={(e) => setDialogueCount(Number(e.target.value))}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div className="flex items-end">
              <Button
                onClick={handleGenerateScenario}
                loading={generating}
                disabled={generating || participants.length === 0}
                variant="success"
                className="w-full"
              >
                시나리오 생성
              </Button>
            </div>
          </div>

          {participants.length === 0 && (
            <p className="text-sm text-amber-600 bg-amber-50 p-3 rounded-md">
              ⚠️ 참여 캐릭터가 없습니다. 장면에 캐릭터를 추가해주세요.
            </p>
          )}
        </Card>

        {/* Generated Dialogues */}
        {generatedDialogues.length > 0 && (
          <Card title="생성된 시나리오" className="mb-6">
            <div className="space-y-3">
              {generatedDialogues.map((dialogue, index) => (
                <div
                  key={index}
                  className="p-4 bg-green-50 border border-green-200 rounded-lg"
                >
                  <div className="flex items-start gap-3">
                    <div className="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-semibold">
                      {dialogue.order}
                    </div>
                    <div className="flex-1">
                      <div className="font-semibold text-gray-800 mb-1">
                        {dialogue.speaker}
                      </div>
                      <p className="text-gray-700">{dialogue.text}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </Card>
        )}

        {/* Existing Dialogues */}
        <Card title="기존 대사" className="mb-6">
          <div className="mb-4">
            <Button onClick={() => setShowAddModal(true)} variant="success">
              + 대사 추가
            </Button>
          </div>
          {dialogues.length > 0 ? (
            <div className="space-y-3">
              {dialogues.map((dialogue) => (
                <div
                  key={dialogue.id}
                  className="p-4 bg-blue-50 border border-blue-200 rounded-lg"
                >
                  {editingDialogue?.id === dialogue.id ? (
                    <div>
                      <textarea
                        value={newDialogueText}
                        onChange={(e) => setNewDialogueText(e.target.value)}
                        className="w-full px-3 py-2 border rounded-lg"
                        rows={3}
                      />
                      <div className="flex gap-2 mt-2">
                        <Button
                          onClick={() => handleUpdateDialogue(dialogue.id, { text: newDialogueText })}
                          variant="success"
                        >
                          저장
                        </Button>
                        <Button onClick={() => setEditingDialogue(null)} variant="secondary">
                          취소
                        </Button>
                      </div>
                    </div>
                  ) : (
                    <div className="flex items-start gap-3">
                      <div className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-semibold">
                        {dialogue.dialogueOrder}
                      </div>
                      <div className="flex-1">
                        <div className="font-semibold text-gray-800 mb-1">
                          {dialogue.character?.name || 'Unknown'}
                        </div>
                        <p className="text-gray-700">{dialogue.text}</p>
                        {dialogue.intent && (
                          <div className="mt-2 text-xs text-gray-500">
                            의도: {dialogue.intent} | 어투: {dialogue.honorific}
                          </div>
                        )}
                      </div>
                      <div className="flex gap-2">
                        <button
                          onClick={() => {
                            setEditingDialogue(dialogue);
                            setNewDialogueText(dialogue.text);
                          }}
                          className="text-blue-600 hover:text-blue-800 text-sm"
                        >
                          수정
                        </button>
                        <button
                          onClick={() => handleDeleteDialogue(dialogue.id)}
                          className="text-red-600 hover:text-red-800 text-sm"
                        >
                          삭제
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <p className="text-gray-500 text-center py-8">
              아직 대사가 없습니다. '대사 추가' 버튼을 눌러 시작하세요.
            </p>
          )}
        </Card>

        {/* 대사 추가 모달 */}
        {showAddModal && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
              <h3 className="text-xl font-bold mb-4">대사 추가</h3>
              <div className="space-y-4">
                <div>
                  <label className="block mb-2 text-sm font-semibold">캐릭터</label>
                  <select
                    value={newDialogue.characterId}
                    onChange={(e) => setNewDialogue({ ...newDialogue, characterId: e.target.value })}
                    className="w-full px-3 py-2 border rounded-lg"
                  >
                    <option value="">선택하세요</option>
                    {participants.map((p) => (
                      <option key={p.characterId} value={p.characterId}>
                        {p.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block mb-2 text-sm font-semibold">대사</label>
                  <textarea
                    value={newDialogue.text}
                    onChange={(e) => setNewDialogue({ ...newDialogue, text: e.target.value })}
                    className="w-full px-3 py-2 border rounded-lg"
                    rows={4}
                    placeholder="대사 내용을 입력하세요"
                  />
                </div>
                <div>
                  <label className="block mb-2 text-sm font-semibold">어투</label>
                  <select
                    value={newDialogue.honorific}
                    onChange={(e) => setNewDialogue({ ...newDialogue, honorific: e.target.value })}
                    className="w-full px-3 py-2 border rounded-lg"
                  >
                    <option value="banmal">반말</option>
                    <option value="jondae">존댓말</option>
                  </select>
                </div>
                <div className="flex gap-2 mt-4">
                  <Button onClick={handleAddDialogue} variant="success" className="flex-1">
                    추가
                  </Button>
                  <Button onClick={() => setShowAddModal(false)} variant="secondary" className="flex-1">
                    취소
                  </Button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Export Actions */}
        <Card title="내보내기 및 버전 관리" className="mb-6">
          <div className="flex flex-wrap gap-3">
            <Button onClick={handleExportText} variant="secondary">
              📄 텍스트 파일로 내보내기
            </Button>
            <Button onClick={handleExportJSON} variant="secondary">
              📦 JSON 파일로 내보내기
            </Button>
            <Button onClick={handleSaveVersion} variant="primary">
              💾 현재 버전 저장
            </Button>
            <Button onClick={() => { fetchVersions(); setShowVersions(true); }} variant="secondary">
              🕐 저장된 버전 보기
            </Button>
          </div>
          <p className="text-sm text-gray-500 mt-3">
            현재 표시된 기존 대사와 생성된 대사를 파일로 내보내거나 버전으로 저장할 수 있습니다.
          </p>
        </Card>

        {/* 버전 관리 모달 */}
        {showVersions && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h3 className="text-xl font-bold">저장된 버전 목록</h3>
                <button
                  onClick={() => setShowVersions(false)}
                  className="text-gray-500 hover:text-gray-700"
                >
                  ✕
                </button>
              </div>
              {versions.length > 0 ? (
                <div className="space-y-3">
                  {versions.map((version) => (
                    <div
                      key={version.id}
                      className="p-4 border border-gray-300 rounded-lg hover:bg-gray-50"
                    >
                      <div className="flex justify-between items-start">
                        <div className="flex-1">
                          <h4 className="font-semibold text-gray-800">{version.title}</h4>
                          <p className="text-sm text-gray-500 mt-1">
                            버전 {version.version} | {new Date(version.createdAt).toLocaleString()}
                          </p>
                        </div>
                        <Button
                          onClick={() => handleLoadVersion(version.id)}
                          variant="primary"
                          className="ml-4"
                        >
                          불러오기
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-gray-500 text-center py-8">
                  저장된 버전이 없습니다.
                </p>
              )}
            </div>
          </div>
        )}
      </div>
    </main>
  );
}
