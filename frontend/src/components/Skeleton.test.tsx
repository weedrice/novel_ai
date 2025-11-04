import React from 'react';
import { render } from '@testing-library/react';
import '@testing-library/jest-dom';
import { Skeleton } from './Skeleton';

describe('Skeleton Component', () => {
  it('renders skeleton loader', () => {
    const { container } = render(<Skeleton />);
    const skeleton = container.querySelector('.skeleton');
    expect(skeleton).toBeInTheDocument();
  });

  it('applies custom width', () => {
    const { container } = render(<Skeleton width="200px" />);
    const skeleton = container.querySelector('.skeleton');
    expect(skeleton).toHaveStyle({ width: '200px' });
  });

  it('applies custom height', () => {
    const { container } = render(<Skeleton height="50px" />);
    const skeleton = container.querySelector('.skeleton');
    expect(skeleton).toHaveStyle({ height: '50px' });
  });

  it('renders multiple skeleton lines', () => {
    const { container } = render(<Skeleton count={3} />);
    const skeletons = container.querySelectorAll('.skeleton');
    expect(skeletons).toHaveLength(3);
  });

  it('applies circle variant', () => {
    const { container } = render(<Skeleton variant="circle" />);
    const skeleton = container.querySelector('.skeleton');
    expect(skeleton).toHaveClass(/circle/i);
  });

  it('applies rectangular variant by default', () => {
    const { container } = render(<Skeleton />);
    const skeleton = container.querySelector('.skeleton');
    expect(skeleton).toBeInTheDocument();
  });
});
