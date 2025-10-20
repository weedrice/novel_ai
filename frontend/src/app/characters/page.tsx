'use client';

import { useState, useEffect } from 'react';

const API = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

interface Character {
  id: number;
  characterId: string;
  name: string;
  description: string;
  personality: string;
}

interface SpeakingProfile {
  speakingStyle: string;
  vocabulary: string;
  toneKeywords: string;
  examples: string;
  prohibitedWords: string;
  sentencePatterns: string;
}

export default function CharactersPage() {
  const [characters, setCharacters] = useState<Character[]>([]);
  const [selectedCharacter, setSelectedCharacter] = useState<Character | null>(null);
  const [profile, setProfile] = useState<SpeakingProfile | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // 캐릭터 목록 로드
  useEffect(() => {
    fetchCharacters();
  }, []);

  const fetchCharacters = async () => {
    try {
      const response = await fetch(`${API}/characters`);
      const data = await response.json();
      setCharacters(data);
    } catch (err) {
      setError('캐릭터 목록을 불러오는데 실패했습니다.');
      console.error(err);
    }
  };

  // 말투 프로필 로드
  const loadSpeakingProfile = async (character: Character) => {
    setSelectedCharacter(character);
    setLoading(true);
    setError(null);
    setSuccessMessage(null);

    try {
      const response = await fetch(`${API}/characters/${character.id}/speaking-profile`);
      const data = await response.json();
      setProfile(data);
    } catch (err) {
      setError('말투 프로필을 불러오는데 실패했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // 말투 프로필 저장
  const saveSpeakingProfile = async () => {
    if (!selectedCharacter || !profile) return;

    setLoading(true);
    setError(null);
    setSuccessMessage(null);

    try {
      const response = await fetch(
        `${API}/characters/${selectedCharacter.id}/speaking-profile`,
        {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(profile),
        }
      );

      if (!response.ok) {
        throw new Error('저장 실패');
      }

      const updated = await response.json();
      setProfile(updated);
      setSuccessMessage('말투 프로필이 성공적으로 저장되었습니다!');
    } catch (err) {
      setError('말투 프로필 저장에 실패했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: 40, maxWidth: 1400, margin: '0 auto' }}>
      <h1 style={{ fontSize: 32, fontWeight: 'bold', marginBottom: 20 }}>
        캐릭터 말투 프로필 관리
      </h1>

      <div style={{ display: 'flex', gap: 30 }}>
        {/* 왼쪽: 캐릭터 목록 */}
        <div style={{ width: 300 }}>
          <h2 style={{ fontSize: 20, fontWeight: 'bold', marginBottom: 15 }}>
            캐릭터 목록
          </h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {characters.map((char) => (
              <button
                key={char.id}
                onClick={() => loadSpeakingProfile(char)}
                style={{
                  padding: '15px',
                  border: selectedCharacter?.id === char.id ? '2px solid #0070f3' : '1px solid #ccc',
                  borderRadius: 8,
                  backgroundColor: selectedCharacter?.id === char.id ? '#e6f2ff' : 'white',
                  cursor: 'pointer',
                  textAlign: 'left',
                  transition: 'all 0.2s',
                }}
              >
                <div style={{ fontWeight: 'bold', marginBottom: 5 }}>{char.name}</div>
                <div style={{ fontSize: 12, color: '#666' }}>{char.characterId}</div>
              </button>
            ))}
          </div>
        </div>

        {/* 오른쪽: 말투 프로필 편집 폼 */}
        <div style={{ flex: 1 }}>
          {!selectedCharacter ? (
            <div style={{ textAlign: 'center', color: '#999', marginTop: 100 }}>
              왼쪽에서 캐릭터를 선택하세요
            </div>
          ) : loading ? (
            <div style={{ textAlign: 'center', color: '#666', marginTop: 100 }}>
              로딩 중...
            </div>
          ) : profile ? (
            <div>
              <h2 style={{ fontSize: 24, fontWeight: 'bold', marginBottom: 20 }}>
                {selectedCharacter.name}의 말투 프로필
              </h2>

              {error && (
                <div style={{ padding: 15, backgroundColor: '#fee', color: '#c00', borderRadius: 8, marginBottom: 20 }}>
                  {error}
                </div>
              )}

              {successMessage && (
                <div style={{ padding: 15, backgroundColor: '#efe', color: '#0a0', borderRadius: 8, marginBottom: 20 }}>
                  {successMessage}
                </div>
              )}

              <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
                {/* 말투 특징 */}
                <div>
                  <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 8 }}>
                    말투 특징 *
                  </label>
                  <textarea
                    value={profile.speakingStyle}
                    onChange={(e) => setProfile({ ...profile, speakingStyle: e.target.value })}
                    rows={3}
                    style={{
                      width: '100%',
                      padding: 10,
                      fontSize: 14,
                      border: '1px solid #ccc',
                      borderRadius: 5,
                      fontFamily: 'inherit',
                    }}
                    placeholder="예: 밝고 긍정적인 어투, 친구처럼 편안한 말투"
                  />
                </div>

                {/* 자주 사용하는 어휘 */}
                <div>
                  <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 8 }}>
                    자주 사용하는 어휘 (쉼표로 구분)
                  </label>
                  <input
                    type="text"
                    value={profile.vocabulary || ''}
                    onChange={(e) => setProfile({ ...profile, vocabulary: e.target.value })}
                    style={{
                      width: '100%',
                      padding: 10,
                      fontSize: 14,
                      border: '1px solid #ccc',
                      borderRadius: 5,
                    }}
                    placeholder="예: 완전, 진짜, 대박, 헐"
                  />
                </div>

                {/* 말투 키워드 */}
                <div>
                  <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 8 }}>
                    말투 키워드 (쉼표로 구분)
                  </label>
                  <input
                    type="text"
                    value={profile.toneKeywords || ''}
                    onChange={(e) => setProfile({ ...profile, toneKeywords: e.target.value })}
                    style={{
                      width: '100%',
                      padding: 10,
                      fontSize: 14,
                      border: '1px solid #ccc',
                      borderRadius: 5,
                    }}
                    placeholder="예: 발랄함, 명랑함, 친근함"
                  />
                </div>

                {/* 대사 예시 */}
                <div>
                  <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 8 }}>
                    대사 예시 (줄바꿈으로 구분)
                  </label>
                  <textarea
                    value={profile.examples || ''}
                    onChange={(e) => setProfile({ ...profile, examples: e.target.value })}
                    rows={5}
                    style={{
                      width: '100%',
                      padding: 10,
                      fontSize: 14,
                      border: '1px solid #ccc',
                      borderRadius: 5,
                      fontFamily: 'inherit',
                    }}
                    placeholder="예:&#10;완전 신나! 우리 같이 가자!&#10;진짜? 대박! 너무 좋은데?&#10;헐, 그게 무슨 소리야?"
                  />
                </div>

                {/* 금지 단어 */}
                <div>
                  <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 8 }}>
                    사용하지 않는 단어 (쉼표로 구분)
                  </label>
                  <input
                    type="text"
                    value={profile.prohibitedWords || ''}
                    onChange={(e) => setProfile({ ...profile, prohibitedWords: e.target.value })}
                    style={{
                      width: '100%',
                      padding: 10,
                      fontSize: 14,
                      border: '1px solid #ccc',
                      borderRadius: 5,
                    }}
                    placeholder="예: 격식체, 존댓말, 비속어"
                  />
                </div>

                {/* 문장 패턴 */}
                <div>
                  <label style={{ display: 'block', fontWeight: 'bold', marginBottom: 8 }}>
                    문장 패턴 (줄바꿈으로 구분)
                  </label>
                  <textarea
                    value={profile.sentencePatterns || ''}
                    onChange={(e) => setProfile({ ...profile, sentencePatterns: e.target.value })}
                    rows={4}
                    style={{
                      width: '100%',
                      padding: 10,
                      fontSize: 14,
                      border: '1px solid #ccc',
                      borderRadius: 5,
                      fontFamily: 'inherit',
                    }}
                    placeholder="예:&#10;~지 뭐야&#10;~인 것 같아&#10;진짜 ~하다"
                  />
                </div>

                {/* 저장 버튼 */}
                <button
                  onClick={saveSpeakingProfile}
                  disabled={loading}
                  style={{
                    padding: '15px 30px',
                    fontSize: 16,
                    fontWeight: 'bold',
                    color: 'white',
                    backgroundColor: loading ? '#ccc' : '#0070f3',
                    border: 'none',
                    borderRadius: 8,
                    cursor: loading ? 'not-allowed' : 'pointer',
                    marginTop: 10,
                  }}
                >
                  {loading ? '저장 중...' : '말투 프로필 저장'}
                </button>
              </div>
            </div>
          ) : null}
        </div>
      </div>
    </div>
  );
}