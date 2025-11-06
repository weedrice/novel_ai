import React from 'react';

/**
 * 공통 Textarea 컴포넌트 (접근성 개선)
 */

export interface TextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
  helperText?: string;
  variant?: 'default' | 'filled' | 'outlined';
  fullWidth?: boolean;
}

const Textarea = React.forwardRef<HTMLTextAreaElement, TextareaProps>(
  (
    {
      label,
      error,
      helperText,
      variant = 'default',
      fullWidth = false,
      className = '',
      id,
      required,
      disabled,
      ...props
    },
    ref
  ) => {
    // 자동 생성된 ID (label과 연결용)
    const autoId = React.useId();
    const textareaId = id || autoId;

    const variants = {
      default: `
        bg-white dark:bg-gray-700
        border border-gray-300 dark:border-gray-600
        focus:border-blue-500 dark:focus:border-blue-400
      `,
      filled: `
        bg-gray-100 dark:bg-gray-800
        border-0
        focus:bg-white dark:focus:bg-gray-700
      `,
      outlined: `
        bg-transparent
        border-2 border-gray-300 dark:border-gray-600
        focus:border-blue-500 dark:focus:border-blue-400
      `,
    };

    const baseClasses = `
      px-4 py-3
      text-gray-900 dark:text-gray-100
      rounded-lg
      focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400
      transition-colors duration-200
      disabled:opacity-50 disabled:cursor-not-allowed
      placeholder:text-gray-400 dark:placeholder:text-gray-500
      resize-y
      ${fullWidth ? 'w-full' : ''}
      ${error ? 'border-red-500 dark:border-red-400' : ''}
      ${variants[variant]}
      ${className}
    `.trim().replace(/\s+/g, ' ');

    return (
      <div className={`${fullWidth ? 'w-full' : ''}`}>
        {label && (
          <label
            htmlFor={textareaId}
            className="block mb-2 text-sm font-medium text-gray-700 dark:text-gray-300"
          >
            {label}
            {required && <span className="text-red-500 ml-1" aria-label="required">*</span>}
          </label>
        )}
        <textarea
          ref={ref}
          id={textareaId}
          className={baseClasses}
          disabled={disabled}
          aria-invalid={error ? 'true' : 'false'}
          aria-describedby={
            error
              ? `${textareaId}-error`
              : helperText
              ? `${textareaId}-helper`
              : undefined
          }
          {...props}
        />
        {error && (
          <p
            id={`${textareaId}-error`}
            className="mt-1 text-sm text-red-600 dark:text-red-400"
            role="alert"
          >
            {error}
          </p>
        )}
        {!error && helperText && (
          <p
            id={`${textareaId}-helper`}
            className="mt-1 text-sm text-gray-500 dark:text-gray-400"
          >
            {helperText}
          </p>
        )}
      </div>
    );
  }
);

Textarea.displayName = 'Textarea';

export default Textarea;
