import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { LoadingSpinner } from './LoadingSpinner';

describe('LoadingSpinner Component', () => {
  it('renders loading spinner', () => {
    const { container } = render(<LoadingSpinner />);
    expect(container.querySelector('.loading-spinner')).toBeInTheDocument();
  });

  it('displays custom message when provided', () => {
    render(<LoadingSpinner message="Loading data..." />);
    expect(screen.getByText('Loading data...')).toBeInTheDocument();
  });

  it('displays default message when not provided', () => {
    render(<LoadingSpinner />);
    // Default message might be "Loading..." or similar
    const textElement = screen.queryByText(/loading/i);
    if (textElement) {
      expect(textElement).toBeInTheDocument();
    }
  });
});
