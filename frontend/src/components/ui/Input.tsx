import React from 'react';

/**
 * 공통 Input 컴포넌트 (접근성 개선)
 */

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
  icon?: React.ReactNode;
  variant?: 'default' | 'filled' | 'outlined';
  fullWidth?: boolean;
}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  (
    {
      label,
      error,
      helperText,
      icon,
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
    const inputId = id || autoId;

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

    const baseStyles = `
      px-4 py-2 rounded-lg
      text-gray-900 dark:text-gray-100
      placeholder:text-gray-500 dark:placeholder:text-gray-400
      focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 dark:focus:ring-blue-400 dark:focus:ring-offset-gray-900
      disabled:opacity-50 disabled:cursor-not-allowed
      transition-colors duration-200
      ${fullWidth ? 'w-full' : ''}
      ${icon ? 'pl-10' : ''}
      ${error ? 'border-red-500 dark:border-red-400 focus:border-red-500 focus:ring-red-500' : ''}
    `;

    return (
      <div className={fullWidth ? 'w-full' : ''}>
        {label && (
          <label
            htmlFor={inputId}
            className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5"
          >
            {label}
            {required && <span className="text-red-500 ml-1" aria-label="필수">*</span>}
          </label>
        )}

        <div className="relative">
          {icon && (
            <div className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500 dark:text-gray-400">
              {icon}
            </div>
          )}

          <input
            ref={ref}
            id={inputId}
            disabled={disabled}
            required={required}
            aria-invalid={!!error}
            aria-describedby={
              error
                ? `${inputId}-error`
                : helperText
                ? `${inputId}-helper`
                : undefined
            }
            className={`
              ${baseStyles}
              ${variants[variant]}
              ${className}
            `}
            {...props}
          />
        </div>

        {error && (
          <p
            id={`${inputId}-error`}
            className="mt-1.5 text-sm text-red-600 dark:text-red-400"
            role="alert"
          >
            {error}
          </p>
        )}

        {helperText && !error && (
          <p
            id={`${inputId}-helper`}
            className="mt-1.5 text-sm text-gray-500 dark:text-gray-400"
          >
            {helperText}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

export default Input;
