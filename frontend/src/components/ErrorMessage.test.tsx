import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { ErrorMessage } from './ErrorMessage';

describe('ErrorMessage Component', () => {
  it('renders error message', () => {
    render(<ErrorMessage message="An error occurred" />);
    expect(screen.getByText('An error occurred')).toBeInTheDocument();
  });

  it('does not render when message is empty', () => {
    const { container } = render(<ErrorMessage message="" />);
    expect(container).toBeEmptyDOMElement();
  });

  it('applies error styling', () => {
    const { container } = render(<ErrorMessage message="Error" />);
    const errorElement = container.firstChild;
    expect(errorElement).toHaveClass(/error/i);
  });
});
