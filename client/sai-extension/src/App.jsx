import { useState } from 'react'
import SidebarHome from './components/SidebarHome'
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
        {tab === 'home' && <SidebarHome />}
        {tab === 'profile' && <ProfilePanel />}
      </div>
    </div>
  )
}

export default App