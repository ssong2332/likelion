import { useState } from 'react'
import SidebarHome from './components/SidebarHome'
import ArchivePanel from './components/ArchivePanel'
import ProfilePanel from './components/ProfilePanel'
import saiLogo from './assets/sai-logo.png'
import './App.css'

function App() {
  const [tab, setTab] = useState('home')

  return (
    <div className="sai-sidebar">
      <header className="sai-sidebar-header">
        <img src={saiLogo} alt="SAI" className="sai-logo-img" />
        <span className="sai-logo-text">SAI</span>
      </header>

      <nav className="sai-tabs">
        {[
          { id: 'home', label: '홈' },
          { id: 'archive', label: '보관함' },
          { id: 'profile', label: '프로필' },
        ].map((t) => (
          <button
            key={t.id}
            className={tab === t.id ? 'sai-tab active' : 'sai-tab'}
            onClick={() => setTab(t.id)}
          >
            {t.label}
          </button>
        ))}
      </nav>

      <div className="sai-sidebar-content">
        <div style={{ display: tab === 'home' ? 'block' : 'none' }}>
          <SidebarHome active={tab === 'home'} />
        </div>
        <div style={{ display: tab === 'archive' ? 'block' : 'none' }}>
          <ArchivePanel active={tab === 'archive'} />
        </div>
        <div style={{ display: tab === 'profile' ? 'block' : 'none' }}>
          <ProfilePanel />
        </div>
      </div>
    </div>
  )
}

export default App