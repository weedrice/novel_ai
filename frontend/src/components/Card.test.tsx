import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { Card } from './Card';

describe('Card Component', () => {
  it('renders children correctly', () => {
    render(
      <Card>
        <div>Test Content</div>
      </Card>
    );

    expect(screen.getByText('Test Content')).toBeInTheDocument();
  });

  it('applies default className', () => {
    const { container } = render(
      <Card>
        <div>Test</div>
      </Card>
    );

    const cardElement = container.firstChild;
    expect(cardElement).toHaveClass('card');
  });

  it('applies custom className', () => {
    const { container } = render(
      <Card className="custom-class">
        <div>Test</div>
      </Card>
    );

    const cardElement = container.firstChild;
    expect(cardElement).toHaveClass('custom-class');
  });
});
