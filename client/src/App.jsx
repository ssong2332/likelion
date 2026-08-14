import React, { useState } from 'react';
import { Settings, Sparkles } from 'lucide-react';
import Editor from './components/editor/Editor';
import ComparePanel from './components/compare/ComparePanel';
import ReplyPanel from './components/reply/ReplyPanel';
import TimezoneWidget from './components/timezone/TimezoneWidget';
import SettingsModal from './components/settings/SettingsModal';
import { analyzeAndRefine } from './api/client';

export default function App() {
  const [text, setText] = useState('');
  const [targetLang, setTargetLang] = useState('en');
  const [loading, setLoading] = useState(false);
  const [refineResult, setRefineResult] = useState(null);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);

  const handleAnalyze = async () => {
    if (!text.trim()) return;
    setLoading(true);
    const data = await analyzeAndRefine({
      originalText: text,
      targetLang: targetLang
    });
    setRefineResult(data);
    setLoading(false);
  };

  return (
    <div className="app-container">
      {/* Header */}
      <header className="app-header">
        <div className="brand-logo">
          <span className="brand-badge">Manyfast</span>
          <span className="brand-title">AI 업무 메시지 어시스턴트</span>
        </div>
        <button
          onClick={() => setIsSettingsOpen(true)}
          style={{
            background: 'none',
            border: '1px solid var(--border-color)',
            padding: '8px 14px',
            borderRadius: 'var(--radius-md)',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            fontSize: '13px',
            fontWeight: 600
          }}
        >
          <Settings size={15} color="var(--text-muted)" />
          설정 및 규칙 (F-5/F-6)
        </button>
      </header>

      {/* Main Workspace */}
      <main className="main-content">
        {/* Left Column: Editor & Timezone */}
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <TimezoneWidget
            isOffHours={refineResult?.timezoneInfo?.isReceiverOffHours}
            nextAvailableTime={refineResult?.timezoneInfo?.nextAvailableCheckingTime}
          />
          <Editor
            text={text}
            setText={setText}
            onAnalyze={handleAnalyze}
            loading={loading}
            targetLang={targetLang}
            setTargetLang={setTargetLang}
          />
        </div>

        {/* Right Column: Compare & Verification */}
        <div>
          <ComparePanel
            result={refineResult}
            originalText={text}
          />
        </div>
      </main>

      {/* Bottom Section: Reply Assistant (F-8) */}
      <div style={{ maxWidth: '1300px', width: '100%', margin: '0 auto', padding: '0 24px 40px 24px' }}>
        <ReplyPanel />
      </div>

      {/* Settings Modal */}
      <SettingsModal
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
      />
    </div>
  );
}
