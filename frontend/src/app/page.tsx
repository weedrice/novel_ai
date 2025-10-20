'use client';

import { useState } from 'react';

const API = process.env.NEXT_PUBLIC_API_BASE || 'http://localhost:8080';

export default function Home() {
  const [episodes, setEpisodes] = useState<any[]>([]);
  const [candidates, setCandidates] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [provider, setProvider] = useState<string>('openai');
  const [availableProviders, setAvailableProviders] = useState<any>(null);

  const fetchEpisodes = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`${API}/episodes`);
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      const data = await response.json();
      setEpisodes(data);
    } catch (err: any) {
      setError(`에피소드 불러오기 실패: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const fetchSuggestions = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`${API}/dialogue/suggest`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          speakerId: 'char.seha',
          targetIds: ['char.jiho'],
          intent: 'reconcile',
          honorific: 'banmal',
          maxLen: 80,
          nCandidates: 3,
          provider: provider,  // LLM 프로바이더 전달
        }),
      });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      const data = await response.json();
      setCandidates(data.candidates || []);
    } catch (err: any) {
      setError(`대사 제안 실패: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main style={{ padding: 40, fontFamily: 'sans-serif', maxWidth: 800 }}>
      <h1>등장인물 대사 톤 보조 시스템</h1>
      <p style={{ color: '#666' }}>Character Dialogue Tone Assistant System</p>

      <div style={{ marginBottom: 20, display: 'flex', gap: 10 }}>
        <a
          href="/graph"
          style={{
            padding: '10px 20px',
            fontSize: 16,
            backgroundColor: '#8b5cf6',
            color: 'white',
            border: 'none',
            borderRadius: 5,
            textDecoration: 'none',
            display: 'inline-block',
          }}
        >
          캐릭터 관계 그래프 보기
        </a>
        <a
          href="/characters"
          style={{
            padding: '10px 20px',
            fontSize: 16,
            backgroundColor: '#f59e0b',
            color: 'white',
            border: 'none',
            borderRadius: 5,
            textDecoration: 'none',
            display: 'inline-block',
          }}
        >
          캐릭터 말투 프로필 관리
        </a>
      </div>

      <hr style={{ margin: '20px 0' }} />

      <section style={{ marginBottom: 30 }}>
        <h2>에피소드 목록</h2>
        <button
          onClick={fetchEpisodes}
          disabled={loading}
          style={{
            padding: '10px 20px',
            fontSize: 16,
            cursor: loading ? 'not-allowed' : 'pointer',
            backgroundColor: '#0070f3',
            color: 'white',
            border: 'none',
            borderRadius: 5,
          }}
        >
          {loading ? '불러오는 중...' : '에피소드 불러오기'}
        </button>
        {episodes.length > 0 && (
          <ul style={{ marginTop: 15 }}>
            {episodes.map((e: any) => (
              <li key={e.id}>
                <strong>ID {e.id}:</strong> {e.title}
              </li>
            ))}
          </ul>
        )}
      </section>

      <section style={{ marginBottom: 30 }}>
        <h2>대사 제안</h2>

        <div style={{ marginBottom: 15 }}>
          <label style={{ display: 'block', marginBottom: 8, fontWeight: 'bold' }}>
            LLM 프로바이더 선택
          </label>
          <select
            value={provider}
            onChange={(e) => setProvider(e.target.value)}
            style={{
              padding: '8px 12px',
              fontSize: 16,
              borderRadius: 5,
              border: '1px solid #ccc',
              backgroundColor: 'white',
              cursor: 'pointer',
              minWidth: 200,
            }}
          >
            <option value="openai">OpenAI GPT</option>
            <option value="claude">Anthropic Claude</option>
            <option value="gemini">Google Gemini</option>
          </select>
          <p style={{ fontSize: 12, color: '#888', marginTop: 5 }}>
            선택한 LLM: {provider === 'openai' ? 'OpenAI GPT' : provider === 'claude' ? 'Anthropic Claude' : 'Google Gemini'}
          </p>
        </div>

        <button
          onClick={fetchSuggestions}
          disabled={loading}
          style={{
            padding: '10px 20px',
            fontSize: 16,
            cursor: loading ? 'not-allowed' : 'pointer',
            backgroundColor: '#10b981',
            color: 'white',
            border: 'none',
            borderRadius: 5,
          }}
        >
          {loading ? '생성 중...' : '대사 제안 받기'}
        </button>
        <p style={{ fontSize: 14, color: '#666', marginTop: 10 }}>
          화자: char.seha | 대상: char.jiho | 의도: reconcile | 어투: banmal
        </p>
        {candidates.length > 0 && (
          <ol style={{ marginTop: 15 }}>
            {candidates.map((c: any, i: number) => (
              <li key={i} style={{ marginBottom: 10 }}>
                {c.text} <span style={{ color: '#888' }}>({c.score?.toFixed?.(2)})</span>
              </li>
            ))}
          </ol>
        )}
      </section>

      {error && (
        <div
          style={{
            padding: 15,
            backgroundColor: '#fee',
            color: '#c00',
            borderRadius: 5,
            marginTop: 20,
          }}
        >
          {error}
        </div>
      )}
    </main>
  );
}
