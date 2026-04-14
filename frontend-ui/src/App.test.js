import { render, screen } from '@testing-library/react';
import axios from 'axios';
import App from './App';

jest.mock('axios', () => ({
  post: jest.fn(),
  get: jest.fn(),
  defaults: {
    headers: {
      common: {}
    }
  }
}));

test('renders application title', () => {
  axios.post.mockResolvedValue({ data: {} });
  axios.get.mockResolvedValue({ data: {} });
  render(<App />);
  const title = screen.getByText(/carbon credit platform/i);
  expect(title).toBeInTheDocument();
});
