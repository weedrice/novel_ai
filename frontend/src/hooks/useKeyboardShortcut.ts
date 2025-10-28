import { useEffect } from 'react';

/**
 * 키보드 단축키를 등록하는 커스텀 훅
 *
 * @param key - 감지할 키 (예: 'k', '/', 'Escape')
 * @param callback - 단축키가 눌렸을 때 실행할 함수
 * @param options - 옵션 (ctrl, shift, alt, meta 등)
 *
 * @example
 * useKeyboardShortcut('k', () => console.log('K pressed'), { ctrl: true });
 * // Ctrl+K 또는 Cmd+K (Mac)
 */
export function useKeyboardShortcut(
  key: string,
  callback: () => void,
  options: {
    ctrl?: boolean;
    shift?: boolean;
    alt?: boolean;
    meta?: boolean;
    enabled?: boolean;
  } = {}
) {
  const { ctrl = false, shift = false, alt = false, meta = false, enabled = true } = options;

  useEffect(() => {
    if (!enabled) return;

    const handleKeyDown = (event: KeyboardEvent) => {
      // Input, Textarea, Select 요소에서는 단축키 무시
      const target = event.target as HTMLElement;
      if (
        target.tagName === 'INPUT' ||
        target.tagName === 'TEXTAREA' ||
        target.tagName === 'SELECT' ||
        target.isContentEditable
      ) {
        return;
      }

      const isMac = navigator.platform.toUpperCase().indexOf('MAC') >= 0;
      const modifierKey = isMac ? event.metaKey : event.ctrlKey;

      // 수정자 키 체크
      const ctrlMatch = ctrl ? modifierKey : !event.ctrlKey && !event.metaKey;
      const shiftMatch = shift ? event.shiftKey : !event.shiftKey;
      const altMatch = alt ? event.altKey : !event.altKey;
      const metaMatch = meta ? event.metaKey : !event.metaKey;

      // 키 체크 (대소문자 구분 없음)
      const keyMatch = event.key.toLowerCase() === key.toLowerCase();

      if (keyMatch && ctrlMatch && shiftMatch && altMatch && metaMatch) {
        event.preventDefault();
        callback();
      }
    };

    window.addEventListener('keydown', handleKeyDown);

    return () => {
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [key, callback, ctrl, shift, alt, meta, enabled]);
}

/**
 * ESC 키를 감지하는 훅
 */
export function useEscapeKey(callback: () => void, enabled: boolean = true) {
  useEffect(() => {
    if (!enabled) return;

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        callback();
      }
    };

    window.addEventListener('keydown', handleKeyDown);

    return () => {
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [callback, enabled]);
}
