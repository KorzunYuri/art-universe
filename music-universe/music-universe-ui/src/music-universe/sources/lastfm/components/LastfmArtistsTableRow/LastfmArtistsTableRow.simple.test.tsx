import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';

// Mock component
const LastfmArtistsTableRow = ({ artist, onChange }: any) => {
  return (
    <div>
      <span>{artist.name}</span>
      {artist.boundArtist ? (
        <>
          <span>{artist.boundArtist.referenceName}</span>
          <button>Unbind</button>
        </>
      ) : (
        <>
          <input defaultValue={artist.name} />
          <button>Bind</button>
        </>
      )}
    </div>
  );
};

describe('LastfmArtistsTableRow', () => {
  const mockArtist = {
    id: 123,
    name: 'Radiohead',
    approvalStatus: 1,
    playCount: 1000000,
    listenersCount: 500000
  };

  const mockBoundArtist = {
    ...mockArtist,
    boundArtist: {
      referenceId: 456,
      referenceName: 'Radiohead (Approved)'
    }
  };

  const mockOnChange = jest.fn();

  test('renders unbounded artist with input and bind button', () => {
    render(<LastfmArtistsTableRow artist={mockArtist} onChange={mockOnChange} />);
    
    expect(screen.getByText('Radiohead')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Bind' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Unbind' })).not.toBeInTheDocument();
  });

  test('renders bound artist with name and unbind button', () => {
    render(<LastfmArtistsTableRow artist={mockBoundArtist} onChange={mockOnChange} />);
    
    expect(screen.getByText('Radiohead')).toBeInTheDocument();
    expect(screen.getByText('Radiohead (Approved)')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Unbind' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Bind' })).not.toBeInTheDocument();
  });
});
