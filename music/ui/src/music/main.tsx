import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import MusicUniverseApp from './MusicUniverseApp'
import { setupGlobalTracingInterceptor } from './shared/services/tracingInterceptor'

// Initialize distributed tracing for all axios requests
setupGlobalTracingInterceptor()

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <BrowserRouter>
            <MusicUniverseApp />
        </BrowserRouter>
    </React.StrictMode>,
)
