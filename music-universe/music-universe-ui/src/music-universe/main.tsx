import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import MusicUniverseApp from './MusicUniverseApp.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <MusicUniverseApp />
  </StrictMode>
)
