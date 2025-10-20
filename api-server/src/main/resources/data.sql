-- 캐릭터 데이터
INSERT INTO characters (character_id, name, description, personality, speaking_style, vocabulary, tone_keywords, created_at, updated_at)
VALUES
    ('char.seha', '세하', '주인공. 밝고 긍정적인 성격', '외향적, 낙천적', '반말, 친근한 어투', '대박,진짜,ㅋㅋ', '밝음,경쾌함', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('char.jiho', '지호', '세하의 친구. 냉정하고 이성적', '내향적, 논리적', '존댓말 섞인 반말', '그러니까,사실', '차분함,신중함', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('char.mina', '미나', '밝고 활발한 후배', '외향적, 사교적', '밝은 반말, 애교', '헐,완전,진짜루', '귀여움,활발함', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 에피소드 데이터
INSERT INTO episodes (title, description, episode_order, created_at, updated_at)
VALUES
    ('ep1 - 첫 만남', '세하와 지호가 처음 만나는 에피소드', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ep2 - 갈등', '세하와 지호 사이에 오해가 생기는 에피소드', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ep3 - 화해', '세하와 지호가 화해하는 에피소드', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 장면 데이터
INSERT INTO scenes (episode_id, scene_number, location, mood, description, participants, created_at, updated_at)
VALUES
    (1, 1, '학교 복도', 'cheerful', '세하가 지호를 처음 만나는 장면', 'char.seha,char.jiho', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, '카페', 'tense', '세하와 지호가 대화하다 오해가 생기는 장면', 'char.seha,char.jiho', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 1, '공원', 'warm', '세하와 지호가 화해하는 장면', 'char.seha,char.jiho,char.mina', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 대사 데이터
INSERT INTO dialogues (scene_id, character_id, text, dialogue_order, intent, honorific, emotion, created_at, updated_at)
VALUES
    (1, 1, '안녕? 나 세하야!', 1, 'greet', 'banmal', 'happy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1, 2, '어, 안녕... 지호라고 해.', 2, 'greet', 'banmal', 'neutral', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, '그게 무슨 소리야? 내가 언제?', 1, 'argue', 'banmal', 'angry', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 2, '아니, 그러니까 네가 그랬잖아.', 2, 'argue', 'banmal', 'frustrated', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 1, '미안해... 내가 오해했어.', 1, 'reconcile', 'banmal', 'sad', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 2, '나도 미안해. 내가 설명을 제대로 못했네.', 2, 'reconcile', 'banmal', 'warm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 관계 데이터
INSERT INTO relationships (from_character_id, to_character_id, relation_type, closeness, description, created_at, updated_at)
VALUES
    (1, 2, 'friend', 8.5, '절친한 친구 사이', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, 'friend', 8.5, '절친한 친구 사이', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1, 3, 'friend', 6.0, '밝은 후배', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 1, 'friend', 7.0, '좋아하는 선배', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);