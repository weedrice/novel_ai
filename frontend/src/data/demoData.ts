/**
 * 데모 데이터
 * 로그인하지 않은 사용자에게 보여줄 예시 데이터
 */

export const demoEpisodes = [
  {
    id: 1,
    title: "첫 만남",
    description: "주인공들이 처음 만나는 장면",
    episodeOrder: 1,
  },
  {
    id: 2,
    title: "갈등의 시작",
    description: "오해로 인한 갈등이 시작됨",
    episodeOrder: 2,
  },
  {
    id: 3,
    title: "화해",
    description: "서로를 이해하고 화해하는 장면",
    episodeOrder: 3,
  },
];

export const demoCharacters = [
  {
    id: 1,
    characterId: "char.seha",
    name: "세하",
    description: "밝고 긍정적인 성격의 주인공. 언제나 친구들을 먼저 생각한다.",
    personality: "밝고 낙천적이며, 친구들에게 헌신적이다. 가끔 너무 앞서나가는 경향이 있다.",
    mbti: "ENFP",
    speakingStyle: "반말, 친근한 말투",
    vocabulary: "야, 진짜, 대박, 완전",
    toneKeywords: "친근함, 활기참, 긍정적",
    examples: "야 지호야! 오늘 날씨 진짜 좋다!\n완전 대박이야! 이거 꼭 해봐야 해!",
    prohibitedWords: "존댓말, 격식체",
    sentencePatterns: "~야, ~지?, ~잖아",
  },
  {
    id: 2,
    characterId: "char.jiho",
    name: "지호",
    description: "차분하고 신중한 성격. 계획적이고 논리적으로 생각한다.",
    personality: "냉정하고 이성적이며, 항상 신중하게 판단한다. 감정 표현이 서툴다.",
    mbti: "INTJ",
    speakingStyle: "존댓말, 정중한 말투",
    vocabulary: "생각해보니, 그렇군요, 합리적",
    toneKeywords: "차분함, 신중함, 이성적",
    examples: "생각해보니 그 방법이 더 합리적일 것 같아요.\n천천히 계획을 세워보는 게 어떨까요?",
    prohibitedWords: "반말, 속어",
    sentencePatterns: "~것 같아요, ~는 게 좋겠어요, ~해보면 어떨까요",
  },
  {
    id: 3,
    characterId: "char.mina",
    name: "미나",
    description: "감성적이고 예술적인 성격. 주변 사람들의 감정을 잘 읽는다.",
    personality: "섬세하고 감수성이 풍부하며, 예술적 재능이 있다. 때로는 우울해지기도 한다.",
    mbti: "INFP",
    speakingStyle: "부드러운 반말",
    vocabulary: "그치, 아마도, 느낌이",
    toneKeywords: "부드러움, 감성적, 따뜻함",
    examples: "그치... 그런 느낌 알 것 같아.\n아마도 네 마음도 비슷하지 않을까?",
    prohibitedWords: "거친 말투, 비속어",
    sentencePatterns: "~것 같아, ~지 않을까, ~라는 느낌",
  },
];

export const demoRelationships = [
  {
    id: 1,
    fromCharacterId: 1,
    toCharacterId: 2,
    fromCharacterName: "세하",
    toCharacterName: "지호",
    relationType: "친구",
    closeness: 8,
    description: "어린 시절부터 함께 자란 절친한 친구",
  },
  {
    id: 2,
    fromCharacterId: 2,
    toCharacterId: 3,
    fromCharacterName: "지호",
    toCharacterName: "미나",
    relationType: "친구",
    closeness: 7,
    description: "같은 동아리에서 만난 친구",
  },
  {
    id: 3,
    fromCharacterId: 1,
    toCharacterId: 3,
    fromCharacterName: "세하",
    toCharacterName: "미나",
    relationType: "친구",
    closeness: 9,
    description: "서로의 꿈을 응원하는 가까운 사이",
  },
];

export const demoScenes = [
  {
    id: 1,
    episodeId: 1,
    sceneNumber: 1,
    location: "학교 운동장",
    mood: "밝고 경쾌함",
    description: "점심시간, 운동장에서 우연히 마주친 세하와 지호",
    participants: "char.seha,char.jiho",
  },
  {
    id: 2,
    episodeId: 1,
    sceneNumber: 2,
    location: "교실",
    mood: "따뜻함",
    description: "방과 후, 미나가 세하와 지호를 만난다",
    participants: "char.seha,char.jiho,char.mina",
  },
  {
    id: 3,
    episodeId: 2,
    sceneNumber: 1,
    location: "카페",
    mood: "긴장감",
    description: "오해가 생긴 후 셋이 만난 자리",
    participants: "char.seha,char.jiho,char.mina",
  },
];

export const demoDialogues = [
  {
    id: 1,
    sceneId: 1,
    characterId: 1,
    characterName: "세하",
    text: "야 지호야! 여기서 뭐해?",
    dialogueOrder: 1,
    honorific: "banmal",
  },
  {
    id: 2,
    sceneId: 1,
    characterId: 2,
    characterName: "지호",
    text: "점심 먹고 산책 중이었어요. 세하는요?",
    dialogueOrder: 2,
    honorific: "jondae",
  },
  {
    id: 3,
    sceneId: 1,
    characterId: 1,
    characterName: "세하",
    text: "나도! 우리 같이 걸을까?",
    dialogueOrder: 3,
    honorific: "banmal",
  },
];

export const demoCandidates = [
  {
    text: "그래요, 좋아요. 날씨도 좋으니까요.",
    score: 0.95,
  },
  {
    text: "물론이죠. 함께 걸어요.",
    score: 0.92,
  },
  {
    text: "좋은 생각이에요. 같이 가요.",
    score: 0.88,
  },
];

/**
 * 데모 모드 여부 확인
 */
export const isDemoMode = (): boolean => {
  if (typeof window === 'undefined') return false;
  const token = localStorage.getItem('token');
  return !token;
};
