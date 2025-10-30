'use client';

import { useState } from 'react';
import Link from 'next/link';
import apiClient from '@/lib/api';

interface ExtractedCharacter {
  name: string;
  description: string;
  personality: string;
  speakingStyle: string;
  dialogueExamples: string[];
}

interface ExtractedDialogue {
  characterName: string;
  text: string;
  sceneNumber: number;
}

interface ExtractedScene {
  sceneNumber: number;
  location: string;
  mood: string;
  description: string;
  participants: string[];
}

interface ExtractedRelationship {
  fromCharacter: string;
  toCharacter: string;
  relationType: string;
  closeness: number;
  description: string;
}

interface AnalysisResult {
  characters: ExtractedCharacter[];
  dialogues: ExtractedDialogue[];
  scenes: ExtractedScene[];
  relationships: ExtractedRelationship[];
}

export default function ScriptAnalyzer() {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [formatHint, setFormatHint] = useState('novel');
  const [provider, setProvider] = useState('openai');
  const [loading, setLoading] = useState(false);
  const [analysis, setAnalysis] = useState<AnalysisResult | null>(null);
  const [error, setError] = useState('');

  const handleAnalyze = async () => {
    if (!title.trim() || !content.trim()) {
      setError('제목과 내용을 모두 입력해주세요.');
      return;
    }

    setLoading(true);
    setError('');
    setAnalysis(null);

    try {
      const response = await apiClient.post('/scripts/upload-and-analyze', {
        title,
        content,
        formatHint,
        provider,
      });

      setAnalysis(response.data.analysis);
      console.log('Analysis result:', response.data);
    } catch (err: any) {
      console.error('Error analyzing script:', err);
      setError(err.message || '분석 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleClear = () => {
    setTitle('');
    setContent('');
    setAnalysis(null);
    setError('');
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-blue-50 dark:from-gray-900 dark:via-gray-900 dark:to-gray-800 p-4 sm:p-6 md:p-8 transition-colors duration-200">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-6 sm:mb-8">
          <div className="flex-1">
            <h1 className="text-2xl sm:text-3xl md:text-4xl font-bold text-gray-800 dark:text-white mb-2">📝 스크립트 분석기</h1>
            <p className="text-sm sm:text-base text-gray-600 dark:text-gray-300">
              소설, 시나리오, 묘사 등 다양한 형식의 스크립트를 분석하여 캐릭터와 관계를 추출합니다
            </p>
          </div>
          <Link
            href="/"
            className="px-4 sm:px-6 py-2 sm:py-3 bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-200 rounded-lg transition-colors text-sm sm:text-base whitespace-nowrap"
          >
            ← 홈으로
          </Link>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6 md:gap-8">
          {/* Input Panel */}
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-4 sm:p-6 transition-colors">
            <h2 className="text-xl sm:text-2xl font-bold text-gray-800 dark:text-white mb-4">📥 스크립트 입력</h2>

            {/* Title Input */}
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">제목</label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="스크립트 제목을 입력하세요"
                className="w-full px-3 sm:px-4 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-purple-500 dark:focus:ring-purple-400 focus:border-transparent transition-colors"
              />
            </div>

            {/* Format and Provider */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">형식</label>
                <select
                  value={formatHint}
                  onChange={(e) => setFormatHint(e.target.value)}
                  className="w-full px-3 sm:px-4 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-purple-500 dark:focus:ring-purple-400 focus:border-transparent transition-colors"
                >
                  <option value="novel">소설</option>
                  <option value="scenario">시나리오</option>
                  <option value="description">묘사</option>
                  <option value="dialogue">대화</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">LLM 프로바이더</label>
                <select
                  value={provider}
                  onChange={(e) => setProvider(e.target.value)}
                  className="w-full px-3 sm:px-4 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-purple-500 dark:focus:ring-purple-400 focus:border-transparent transition-colors"
                >
                  <option value="openai">OpenAI GPT</option>
                  <option value="claude">Anthropic Claude</option>
                  <option value="gemini">Google Gemini</option>
                </select>
              </div>
            </div>

            {/* Content Textarea */}
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                내용 ({content.length} 자)
              </label>
              <textarea
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="스크립트 내용을 입력하세요...

예시 (소설):
&quot;안녕?&quot; 세하가 복도에서 지호를 보고 손을 흔들었다.
&quot;어, 오랜만이야!&quot; 지호가 환하게 웃으며 대답했다.

예시 (시나리오):
세하: 안녕? 오랜만이야!
지호: 어, 세하야! 잘 지냈어?

예시 (묘사):
학교 복도에서 세하와 지호가 만났다. 세하는 밝은 성격의 고등학생이고, 지호는 세하의 오랜 친구다."
                rows={12}
                className="w-full px-3 sm:px-4 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-purple-500 dark:focus:ring-purple-400 focus:border-transparent font-mono text-sm transition-colors"
              />
            </div>

            {/* Action Buttons */}
            <div className="flex flex-col sm:flex-row gap-3">
              <button
                onClick={handleAnalyze}
                disabled={loading}
                className="flex-1 px-4 sm:px-6 py-2 sm:py-3 bg-purple-600 hover:bg-purple-700 dark:bg-purple-500 dark:hover:bg-purple-600 disabled:bg-gray-400 dark:disabled:bg-gray-600 text-white font-semibold rounded-lg transition-colors text-sm sm:text-base"
              >
                {loading ? '분석 중...' : '🔍 분석 시작'}
              </button>
              <button
                onClick={handleClear}
                disabled={loading}
                className="px-4 sm:px-6 py-2 sm:py-3 bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 disabled:bg-gray-100 dark:disabled:bg-gray-800 text-gray-700 dark:text-gray-200 font-semibold rounded-lg transition-colors text-sm sm:text-base"
              >
                초기화
              </button>
            </div>

            {/* Error Message */}
            {error && (
              <div className="mt-4 p-3 sm:p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                <p className="text-sm sm:text-base text-red-700 dark:text-red-400">⚠️ {error}</p>
              </div>
            )}
          </div>

          {/* Analysis Results Panel */}
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-4 sm:p-6 overflow-y-auto max-h-[600px] sm:max-h-[800px] transition-colors">
            <h2 className="text-xl sm:text-2xl font-bold text-gray-800 dark:text-white mb-4">📊 분석 결과</h2>

            {!analysis && !loading && (
              <div className="text-center py-12 sm:py-20 text-gray-400 dark:text-gray-500">
                <p className="text-base sm:text-lg">스크립트를 입력하고 분석을 시작하세요</p>
              </div>
            )}

            {loading && (
              <div className="text-center py-12 sm:py-20">
                <div className="animate-spin w-12 h-12 sm:w-16 sm:h-16 border-4 border-purple-500 dark:border-purple-400 border-t-transparent rounded-full mx-auto mb-4"></div>
                <p className="text-sm sm:text-base text-gray-600 dark:text-gray-300">AI가 스크립트를 분석하고 있습니다...</p>
              </div>
            )}

            {analysis && (
              <div className="space-y-4 sm:space-y-6">
                {/* Characters */}
                <div>
                  <h3 className="text-lg sm:text-xl font-bold text-purple-700 dark:text-purple-400 mb-3 flex items-center gap-2">
                    👥 캐릭터 ({analysis.characters.length})
                  </h3>
                  {analysis.characters.length === 0 ? (
                    <p className="text-sm sm:text-base text-gray-500 dark:text-gray-400 italic">추출된 캐릭터가 없습니다</p>
                  ) : (
                    <div className="space-y-3">
                      {analysis.characters.map((char, idx) => (
                        <div key={idx} className="p-3 sm:p-4 bg-purple-50 dark:bg-purple-900/20 border border-purple-200 dark:border-purple-800 rounded-lg transition-colors">
                          <h4 className="font-bold text-lg text-purple-900">{char.name}</h4>
                          {char.description && (
                            <p className="text-sm text-gray-700 mt-1">{char.description}</p>
                          )}
                          {char.personality && (
                            <p className="text-sm text-gray-600 mt-1">
                              <strong>성격:</strong> {char.personality}
                            </p>
                          )}
                          {char.speakingStyle && (
                            <p className="text-sm text-gray-600 mt-1">
                              <strong>말투:</strong> {char.speakingStyle}
                            </p>
                          )}
                          {char.dialogueExamples.length > 0 && (
                            <div className="mt-2">
                              <strong className="text-sm text-gray-600">대사 예시:</strong>
                              <ul className="list-disc list-inside text-sm text-gray-600 mt-1">
                                {char.dialogueExamples.slice(0, 3).map((ex, i) => (
                                  <li key={i}>&quot;{ex}&quot;</li>
                                ))}
                              </ul>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* Scenes */}
                <div>
                  <h3 className="text-xl font-bold text-blue-700 mb-3 flex items-center gap-2">
                    🎬 장면 ({analysis.scenes.length})
                  </h3>
                  {analysis.scenes.length === 0 ? (
                    <p className="text-gray-500 italic">추출된 장면이 없습니다</p>
                  ) : (
                    <div className="space-y-3">
                      {analysis.scenes.map((scene, idx) => (
                        <div key={idx} className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
                          <h4 className="font-bold text-blue-900">
                            장면 {scene.sceneNumber}: {scene.location || '미지정'}
                          </h4>
                          {scene.mood && (
                            <p className="text-sm text-gray-600 mt-1">
                              <strong>분위기:</strong> {scene.mood}
                            </p>
                          )}
                          {scene.description && (
                            <p className="text-sm text-gray-700 mt-1">{scene.description}</p>
                          )}
                          {scene.participants.length > 0 && (
                            <p className="text-sm text-gray-600 mt-1">
                              <strong>참여:</strong> {scene.participants.join(', ')}
                            </p>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* Dialogues */}
                <div>
                  <h3 className="text-xl font-bold text-green-700 mb-3 flex items-center gap-2">
                    💬 대사 ({analysis.dialogues.length})
                  </h3>
                  {analysis.dialogues.length === 0 ? (
                    <p className="text-gray-500 italic">추출된 대사가 없습니다</p>
                  ) : (
                    <div className="space-y-2 max-h-60 overflow-y-auto">
                      {analysis.dialogues.slice(0, 20).map((dialogue, idx) => (
                        <div key={idx} className="p-3 bg-green-50 border border-green-200 rounded-lg">
                          <p className="text-sm">
                            <strong className="text-green-900">{dialogue.characterName}:</strong>{' '}
                            <span className="text-gray-700">&quot;{dialogue.text}&quot;</span>
                            <span className="text-xs text-gray-500 ml-2">(장면 {dialogue.sceneNumber})</span>
                          </p>
                        </div>
                      ))}
                      {analysis.dialogues.length > 20 && (
                        <p className="text-sm text-gray-500 text-center">
                          ...외 {analysis.dialogues.length - 20}개 대사
                        </p>
                      )}
                    </div>
                  )}
                </div>

                {/* Relationships */}
                <div>
                  <h3 className="text-xl font-bold text-pink-700 mb-3 flex items-center gap-2">
                    💞 관계 ({analysis.relationships.length})
                  </h3>
                  {analysis.relationships.length === 0 ? (
                    <p className="text-gray-500 italic">추출된 관계가 없습니다</p>
                  ) : (
                    <div className="space-y-3">
                      {analysis.relationships.map((rel, idx) => (
                        <div key={idx} className="p-4 bg-pink-50 border border-pink-200 rounded-lg">
                          <p className="font-bold text-pink-900">
                            {rel.fromCharacter} → {rel.toCharacter}
                          </p>
                          <p className="text-sm text-gray-700 mt-1">
                            <strong>관계:</strong> {rel.relationType} (친밀도: {rel.closeness}/10)
                          </p>
                          {rel.description && (
                            <p className="text-sm text-gray-600 mt-1">{rel.description}</p>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* TODO: Add buttons to register characters, scenes, etc. to DB */}
                <div className="pt-4 border-t border-gray-200">
                  <p className="text-sm text-gray-500 text-center">
                    💡 향후 업데이트: 분석 결과를 데이터베이스에 일괄 등록하는 기능이 추가될 예정입니다
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}