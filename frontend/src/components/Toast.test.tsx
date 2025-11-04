import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { Toast } from './Toast';

describe('Toast Component', () => {
  it('renders toast with message', () => {
    render(<Toast message="Test notification" type="success" />);
    expect(screen.getByText('Test notification')).toBeInTheDocument();
  });

  it('applies success type styling', () => {
    const { container } = render(<Toast message="Success" type="success" />);
    const toast = container.querySelector('.toast');
    expect(toast).toHaveClass(/success/i);
  });

  it('applies error type styling', () => {
    const { container } = render(<Toast message="Error" type="error" />);
    const toast = container.querySelector('.toast');
    expect(toast).toHaveClass(/error/i);
  });

  it('applies info type styling', () => {
    const { container } = render(<Toast message="Info" type="info" />);
    const toast = container.querySelector('.toast');
    expect(toast).toHaveClass(/info/i);
  });

  it('applies warning type styling', () => {
    const { container } = render(<Toast message="Warning" type="warning" />);
    const toast = container.querySelector('.toast');
    expect(toast).toHaveClass(/warning/i);
  });

  it('does not render without message', () => {
    const { container } = render(<Toast message="" type="info" />);
    expect(container).toBeEmptyDOMElement();
  });

  it('shows toast with title', () => {
    render(<Toast message="Message" type="info" title="Title" />);
    expect(screen.getByText('Title')).toBeInTheDocument();
    expect(screen.getByText('Message')).toBeInTheDocument();
  });
});
