'use client';

import { useState, useEffect } from 'react';
import Card from '@/components/Card';
import Button from '@/components/ui/Button';
import ErrorMessage from '@/components/ErrorMessage';
import apiClient from '@/lib/api';
import { isDemoMode } from '@/data/demoData';
import Link from 'next/link';

interface Character {
  id: number;
  characterId: string;
  name: string;
  personality?: string;
  speakingStyle?: string;
}

/**
 * Task 92: 스트리밍 대사 생성 데모 페이지
 * Server-Sent Events를 사용한 실시간 LLM 대사 생성
 */
export default function DialogueStreamPage() {
  const [characters, setCharacters] = useState<Character[]>([]);
  const [selectedCharacterId, setSelectedCharacterId] = useState('');
  const [intent, setIntent] = useState('greet');
  const [honorific, setHonorific] = useState('banmal');
  const [provider, setProvider] = useState('openai');

  const [streamingText, setStreamingText] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [streamingStatus, setStreamingStatus] = useState<'idle' | 'connecting' | 'streaming' | 'done' | 'error'>('idle');
  const [isDemo, setIsDemo] = useState(false);

  useEffect(() => {
    const demo = isDemoMode();
    setIsDemo(demo);

    if (!demo) {
      fetchCharacters();
    }
  }, []);

  const fetchCharacters = async () => {
    try {
      const response = await apiClient.get('/characters');
      const data = response.data;
      setCharacters(data);
      if (data.length > 0) {
        setSelectedCharacterId(data[0].characterId);
      }
    } catch (err: any) {
      setError(`캐릭터 목록 불러오기 실패: ${err.message}`);
    }
  };

  const handleStartStreaming = async () => {
    if (!selectedCharacterId) {
      setError('캐릭터를 선택해주세요.');
      return;
    }

    setStreamingText('');
    setError(null);
    setIsStreaming(true);
    setStreamingStatus('connecting');

    try {
      const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE || 'http://localhost:8080';
      const url = `${apiBaseUrl}/dialogue/suggest-stream`;

      const requestBody = {
        speakerId: selectedCharacterId,
        targetIds: [],
        intent: intent,
        honorific: honorific,
        maxLen: 150,
        nCandidates: 1,
        provider: provider
      };

      // EventSource는 GET만 지원하므로 fetch로 직접 구현
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream',
        },
        body: JSON.stringify(requestBody),
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      setStreamingStatus('streaming');
      const reader = response.body?.getReader();
      const decoder = new TextDecoder();

      if (!reader) {
        throw new Error('Response body is null');
      }

      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();

        if (done) {
          setStreamingStatus('done');
          break;
        }

        // 청크를 디코딩하고 버퍼에 추가
        buffer += decoder.decode(value, { stream: true });

        // SSE 메시지 파싱 (data: {...}\n\n 형식)
        const lines = buffer.split('\n\n');
        buffer = lines.pop() || ''; // 마지막 불완전한 줄은 버퍼에 유지

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            try {
              const jsonStr = line.substring(6);
              const event = JSON.parse(jsonStr);

              if (event.type === 'start') {
                console.log('Streaming started:', event.message);
              } else if (event.type === 'chunk') {
                setStreamingText(prev => prev + event.text);
              } else if (event.type === 'done') {
                console.log('Streaming completed:', event.message);
                setStreamingStatus('done');
              } else if (event.type === 'error') {
                console.error('Streaming error:', event.message);
                setError(event.message);
                setStreamingStatus('error');
              }
            } catch (parseError) {
              console.error('Failed to parse SSE event:', line, parseError);
            }
          }
        }
      }
    } catch (err: any) {
      console.error('Streaming error:', err);
      setError(`스트리밍 실패: ${err.message}`);
      setStreamingStatus('error');
    } finally {
      setIsStreaming(false);
    }
  };

  const handleStopStreaming = () => {
    // 실제로는 AbortController를 사용하여 fetch를 중단해야 함
    setIsStreaming(false);
    setStreamingStatus('idle');
  };

  return (
    <div className="container mx-auto p-6">
      {/* 데모 모드 배너 */}
      {isDemo && (
        <div className="mb-6 p-4 bg-blue-50 border-l-4 border-blue-500 rounded-r-lg">
          <p className="text-sm text-blue-700">
            <strong className="font-semibold">데모 모드</strong> - 이 기능을 사용하려면{" "}
            <Link href="/login" className="underline hover:text-blue-800">
              로그인
            </Link>
            해주세요.
          </p>
        </div>
      )}

      <div className="mb-6">
        <h1 className="text-3xl font-bold mb-2">실시간 대사 생성 (스트리밍)</h1>
        <p className="text-gray-600">
          Server-Sent Events를 사용하여 LLM이 대사를 생성하는 과정을 실시간으로 확인할 수 있습니다.
        </p>
      </div>

      {error && (
        <div className="mb-4">
          <ErrorMessage message={error} />
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 설정 패널 */}
        <Card>
          <h2 className="text-xl font-semibold mb-4">생성 설정</h2>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2">
                화자 캐릭터
              </label>
              <select
                value={selectedCharacterId}
                onChange={(e) => setSelectedCharacterId(e.target.value)}
                className="w-full p-2 border border-gray-300 rounded"
                disabled={isStreaming}
              >
                {characters.map((char) => (
                  <option key={char.characterId} value={char.characterId}>
                    {char.name} ({char.characterId})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">
                의도 (Intent)
              </label>
              <select
                value={intent}
                onChange={(e) => setIntent(e.target.value)}
                className="w-full p-2 border border-gray-300 rounded"
                disabled={isStreaming}
              >
                <option value="greet">인사</option>
                <option value="comfort">위로</option>
                <option value="argue">논쟁</option>
                <option value="reconcile">화해</option>
                <option value="thank">감사</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">
                존댓말 수준
              </label>
              <select
                value={honorific}
                onChange={(e) => setHonorific(e.target.value)}
                className="w-full p-2 border border-gray-300 rounded"
                disabled={isStreaming}
              >
                <option value="banmal">반말</option>
                <option value="jondae">존댓말</option>
                <option value="mixed">혼용</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">
                LLM 프로바이더
              </label>
              <select
                value={provider}
                onChange={(e) => setProvider(e.target.value)}
                className="w-full p-2 border border-gray-300 rounded"
                disabled={isStreaming}
              >
                <option value="openai">OpenAI GPT</option>
                <option value="claude">Anthropic Claude</option>
                <option value="gemini">Google Gemini</option>
              </select>
            </div>

            <div className="pt-4">
              {!isStreaming ? (
                <Button
                  onClick={handleStartStreaming}
                  variant="primary"
                  className="w-full"
                  disabled={!selectedCharacterId}
                >
                  스트리밍 시작
                </Button>
              ) : (
                <Button
                  onClick={handleStopStreaming}
                  variant="secondary"
                  className="w-full"
                >
                  중지
                </Button>
              )}
            </div>

            <div className="pt-2 text-sm text-gray-600">
              <p className="font-medium">상태: {
                streamingStatus === 'idle' ? '대기 중' :
                streamingStatus === 'connecting' ? '연결 중...' :
                streamingStatus === 'streaming' ? '생성 중...' :
                streamingStatus === 'done' ? '완료' :
                '오류'
              }</p>
            </div>
          </div>
        </Card>

        {/* 결과 패널 */}
        <Card>
          <h2 className="text-xl font-semibold mb-4">생성 결과 (실시간)</h2>

          <div className="min-h-[300px] bg-gray-50 p-4 rounded border border-gray-200">
            {streamingText ? (
              <div className="whitespace-pre-wrap font-mono text-sm">
                {streamingText}
                {isStreaming && <span className="animate-pulse">▌</span>}
              </div>
            ) : (
              <p className="text-gray-400 italic">
                {isStreaming
                  ? '생성 중...'
                  : '스트리밍 시작 버튼을 눌러 대사를 생성하세요.'
                }
              </p>
            )}
          </div>

          {streamingText && !isStreaming && (
            <div className="mt-4">
              <Button
                onClick={() => {
                  navigator.clipboard.writeText(streamingText);
                  alert('클립보드에 복사되었습니다!');
                }}
                variant="secondary"
                className="w-full"
              >
                클립보드에 복사
              </Button>
            </div>
          )}
        </Card>
      </div>

      <div className="mt-6 p-4 bg-blue-50 rounded border border-blue-200">
        <h3 className="font-semibold text-blue-900 mb-2">📖 사용 방법</h3>
        <ul className="text-sm text-blue-800 space-y-1 list-disc list-inside">
          <li>화자 캐릭터를 선택하세요</li>
          <li>의도, 존댓말 수준, LLM 프로바이더를 설정하세요</li>
          <li>"스트리밍 시작" 버튼을 클릭하면 실시간으로 대사가 생성됩니다</li>
          <li>생성이 완료되면 결과를 클립보드에 복사할 수 있습니다</li>
        </ul>
      </div>
    </div>
  );
}
