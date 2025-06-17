import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { LastfmArtistsTableRow } from './LastfmArtistsTableRow';
import '@testing-library/jest-dom';

// Mock the API functions
jest.mock('@/music-universe/sources/music-data/api/music-data-artists', () => ({
  bindArtist: jest.fn().mockResolvedValue({
    externalId: 123,
    dataSource: 'LASTFM',
    referenceId: 456,
    referenceName: 'Radiohead (Approved)'
  }),
  unbindArtist: jest.fn().mockResolvedValue(true)
}));

jest.mock('@/music-universe/sources/lastfm/api/lastfm-artists', () => ({
  updateArtistApprovalStatus: jest.fn().mockResolvedValue({
    id: 123,
    name: 'Radiohead',
    approvalStatus: 0
  })
}));

// Mock the shared components
jest.mock('@/music-universe/shared/components', () => ({
  ExternalLink: ({ label }: { label: string }) => <span>{label}</span>,
  ReadonlyAttr: ({ value }: { value: number }) => <span>{value}</span>
}));

// Mock the ApprovalToggle component
jest.mock('@/music-universe/sources/lastfm/components', () => ({
  ApprovalToggle: ({ status, onChange }: { status: number, onChange: (status: number) => void }) => (
    <div>
      <button onClick={() => onChange(1)}>yes</button>
      <button onClick={() => onChange(0)}>no</button>
    </div>
  )
}));

describe('LastfmArtistsTableRow', () => {
  const mockArtist = {
    id: 123,
    name: 'Radiohead',
    mbid: 'a74b1b7f-71a5-4011-9441-d0b5e4122711',
    url: 'https://www.last.fm/music/Radiohead',
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

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders unbounded artist with input and bind button', () => {
    render(<LastfmArtistsTableRow artist={mockArtist} onChange={mockOnChange} />);
    
    expect(screen.getByDisplayValue('Radiohead')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Bind' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Unbind' })).not.toBeInTheDocument();
  });

  test('renders bound artist with name and unbind button', () => {
    render(<LastfmArtistsTableRow artist={mockBoundArtist} onChange={mockOnChange} />);
    
    expect(screen.getByText('Radiohead (Approved)')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Unbind' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Bind' })).not.toBeInTheDocument();
  });

  test('updates input value when typing', () => {
    render(<LastfmArtistsTableRow artist={mockArtist} onChange={mockOnChange} />);
    
    const input = screen.getByDisplayValue('Radiohead');
    fireEvent.change(input, { target: { value: 'Radiohead Updated' } });
    
    expect(screen.getByDisplayValue('Radiohead Updated')).toBeInTheDocument();
  });

  test('disables bind button when input is empty', () => {
    render(<LastfmArtistsTableRow artist={mockArtist} onChange={mockOnChange} />);
    
    const input = screen.getByDisplayValue('Radiohead');
    fireEvent.change(input, { target: { value: '' } });
    
    expect(screen.getByRole('button', { name: 'Bind' })).toBeDisabled();
  });
});
