-- Phase 6: 사용자, 프로젝트 기반 초기 데이터
-- 테스트용 사용자 및 샘플 프로젝트 데이터

-- 1. 테스트 사용자 생성
-- username: demo, password: password (BCrypt 해시)
INSERT INTO users (username, email, password, role, created_at, updated_at)
VALUES ('demo', 'demo@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 2. 테스트 프로젝트 생성 (demo 사용자의 첫 프로젝트)
INSERT INTO projects (name, description, owner_id, created_at, updated_at)
SELECT '데모 프로젝트', '초기 테스트용 샘플 프로젝트', u.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u WHERE u.username = 'demo'
ON CONFLICT DO NOTHING;

-- 3. 캐릭터 데이터 (데모 프로젝트)
INSERT INTO characters (character_id, name, description, personality, speaking_style, vocabulary, tone_keywords, examples, prohibited_words, sentence_patterns, project_id, created_at, updated_at)
SELECT 'char.seha', '세하', '주인공. 밝고 긍정적인 성격', '외향적, 낙천적', '반말, 친근한 어투', '대박,진짜,ㅋㅋ', '밝음,경쾌함',
     '안녕? 나 세하야!
오늘 날씨 진짜 좋다!
대박! 이거 완전 재밌는데?
ㅋㅋ 그러게! 나도 그렇게 생각해.',
     '~요,~습니다,~네요',
     '~야!
~지 뭐!
대박 ~!
완전 ~!',
     p.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM projects p WHERE p.name = '데모 프로젝트'
ON CONFLICT (character_id) DO NOTHING;

INSERT INTO characters (character_id, name, description, personality, speaking_style, vocabulary, tone_keywords, examples, prohibited_words, sentence_patterns, project_id, created_at, updated_at)
SELECT 'char.jiho', '지호', '세하의 친구. 냉정하고 이성적', '내향적, 논리적', '존댓말 섞인 반말', '그러니까,사실', '차분함,신중함',
     '어, 안녕... 지호라고 해.
그러니까, 내 말은 그게 아니라...
사실 그건 좀 다르게 봐야 할 것 같아.
아니, 논리적으로 생각해봐.',
     'ㅋㅋ,대박,헐',
     '그러니까 ~
사실은 ~
~라고 생각해
논리적으로 ~',
     p.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM projects p WHERE p.name = '데모 프로젝트'
ON CONFLICT (character_id) DO NOTHING;

INSERT INTO characters (character_id, name, description, personality, speaking_style, vocabulary, tone_keywords, examples, prohibited_words, sentence_patterns, project_id, created_at, updated_at)
SELECT 'char.mina', '미나', '밝고 활발한 후배', '외향적, 사교적', '밝은 반말, 애교', '헐,완전,진짜루', '귀여움,활발함',
     '헐! 선배님~ 오랜만이에요!
완전 좋아요! 진짜루!
어머 대박! 그거 재밌겠다~
히히, 알았어요 선배!',
     '그러니까,논리적',
     '헐 ~!
완전 ~!
~네요~
히히, ~',
     p.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM projects p WHERE p.name = '데모 프로젝트'
ON CONFLICT (character_id) DO NOTHING;

-- 4. 에피소드 데이터 (데모 프로젝트)
WITH project AS (SELECT id FROM projects WHERE name = '데모 프로젝트')
INSERT INTO episodes (title, description, episode_order, project_id, created_at, updated_at)
SELECT 'ep1 - 첫 만남', '세하와 지호가 처음 만나는 에피소드', 1, p.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM project p
UNION ALL
SELECT 'ep2 - 갈등', '세하와 지호 사이에 오해가 생기는 에피소드', 2, p.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM project p
UNION ALL
SELECT 'ep3 - 화해', '세하와 지호가 화해하는 에피소드', 3, p.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM project p;

-- 5. 장면 데이터
INSERT INTO scenes (episode_id, scene_number, location, mood, description, participants, created_at, updated_at)
SELECT e.id, 1, '학교 복도', 'cheerful', '세하가 지호를 처음 만나는 장면', 'char.seha,char.jiho', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM episodes e WHERE e.title = 'ep1 - 첫 만남'
UNION ALL
SELECT e.id, 1, '카페', 'tense', '세하와 지호가 대화하다 오해가 생기는 장면', 'char.seha,char.jiho', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM episodes e WHERE e.title = 'ep2 - 갈등'
UNION ALL
SELECT e.id, 1, '공원', 'warm', '세하와 지호가 화해하는 장면', 'char.seha,char.jiho,char.mina', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM episodes e WHERE e.title = 'ep3 - 화해';

-- 6. 대사 데이터
INSERT INTO dialogues (scene_id, character_id, text, dialogue_order, intent, honorific, emotion, created_at, updated_at)
SELECT s.id, c.id, '안녕? 나 세하야!', 1, 'greet', 'banmal', 'happy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM scenes s
JOIN episodes e ON s.episode_id = e.id
JOIN characters c ON c.character_id = 'char.seha'
WHERE e.title = 'ep1 - 첫 만남' AND s.scene_number = 1
UNION ALL
SELECT s.id, c.id, '어, 안녕... 지호라고 해.', 2, 'greet', 'banmal', 'neutral', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM scenes s
JOIN episodes e ON s.episode_id = e.id
JOIN characters c ON c.character_id = 'char.jiho'
WHERE e.title = 'ep1 - 첫 만남' AND s.scene_number = 1
UNION ALL
SELECT s.id, c.id, '그게 무슨 소리야? 내가 언제?', 1, 'argue', 'banmal', 'angry', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM scenes s
JOIN episodes e ON s.episode_id = e.id
JOIN characters c ON c.character_id = 'char.seha'
WHERE e.title = 'ep2 - 갈등' AND s.scene_number = 1
UNION ALL
SELECT s.id, c.id, '아니, 그러니까 네가 그랬잖아.', 2, 'argue', 'banmal', 'frustrated', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM scenes s
JOIN episodes e ON s.episode_id = e.id
JOIN characters c ON c.character_id = 'char.jiho'
WHERE e.title = 'ep2 - 갈등' AND s.scene_number = 1
UNION ALL
SELECT s.id, c.id, '미안해... 내가 오해했어.', 1, 'reconcile', 'banmal', 'sad', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM scenes s
JOIN episodes e ON s.episode_id = e.id
JOIN characters c ON c.character_id = 'char.seha'
WHERE e.title = 'ep3 - 화해' AND s.scene_number = 1
UNION ALL
SELECT s.id, c.id, '나도 미안해. 내가 설명을 제대로 못했네.', 2, 'reconcile', 'banmal', 'warm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM scenes s
JOIN episodes e ON s.episode_id = e.id
JOIN characters c ON c.character_id = 'char.jiho'
WHERE e.title = 'ep3 - 화해' AND s.scene_number = 1;

-- 7. 관계 데이터
-- CharacterRelationship는 Neo4j Graph DB에서 관리됨
-- PostgreSQL에는 relationships 테이블이 없으므로 주석 처리

-- 데모 계정 정보:
-- Username: demo
-- Password: password
-- Project: 데모 프로젝트
