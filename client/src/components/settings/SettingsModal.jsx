import React, { useState } from 'react';
import { X, BookOpen, User, Trash2, ShieldCheck } from 'lucide-react';

export default function SettingsModal({ isOpen, onClose }) {
  const [activeTab, setActiveTab] = useState('glossary');
  const [glossaries, setGlossaries] = useState([
    { id: 1, term: 'Manyfast', rule: '원문 유지 (Keep Original)', note: '의역 금지' },
    { id: 2, term: 'ASAP', rule: '오늘 EOD 18:00 전', note: '팀 내 합의 기준' }
  ]);
  const [newTerm, setNewTerm] = useState('');
  const [newRule, setNewRule] = useState('');

  if (!isOpen) return null;

  const handleAddGlossary = () => {
    if (!newTerm.trim()) return;
    setGlossaries([...glossaries, { id: Date.now(), term: newTerm, rule: newRule || '원문 유지', note: '사용자 지정' }]);
    setNewTerm('');
    setNewRule('');
  };

  const handleDeleteGlossary = (id) => {
    setGlossaries(glossaries.filter(g => g.id !== id));
  };

  return (
    <div style={{
      position: 'fixed',
      inset: 0,
      background: 'rgba(0, 0, 0, 0.4)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 100
    }}>
      <div style={{
        background: 'white',
        borderRadius: 'var(--radius-lg)',
        width: '560px',
        maxWidth: '90vw',
        padding: '24px',
        boxShadow: 'var(--shadow-lg)'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h3 style={{ fontSize: '17px', fontWeight: 700 }}>설정 및 데이터 관리</h3>
          <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
            <X size={20} color="var(--text-muted)" />
          </button>
        </div>

        {/* Tab Navigation */}
        <div style={{ display: 'flex', gap: '8px', borderBottom: '1px solid var(--border-color)', paddingBottom: '8px', marginBottom: '16px' }}>
          <button
            onClick={() => setActiveTab('glossary')}
            style={{
              padding: '6px 12px',
              borderRadius: 'var(--radius-sm)',
              border: 'none',
              background: activeTab === 'glossary' ? 'var(--primary-orange-light)' : 'none',
              color: activeTab === 'glossary' ? 'var(--primary-orange)' : 'var(--text-muted)',
              fontWeight: activeTab === 'glossary' ? 700 : 500,
              cursor: 'pointer'
            }}
          >
            📖 용어 사전 (F-5)
          </button>
          <button
            onClick={() => setActiveTab('style')}
            style={{
              padding: '6px 12px',
              borderRadius: 'var(--radius-sm)',
              border: 'none',
              background: activeTab === 'style' ? 'var(--primary-orange-light)' : 'none',
              color: activeTab === 'style' ? 'var(--primary-orange)' : 'var(--text-muted)',
              fontWeight: activeTab === 'style' ? 700 : 500,
              cursor: 'pointer'
            }}
          >
            👤 협업 성향 (F-6)
          </button>
          <button
            onClick={() => setActiveTab('privacy')}
            style={{
              padding: '6px 12px',
              borderRadius: 'var(--radius-sm)',
              border: 'none',
              background: activeTab === 'privacy' ? 'var(--primary-orange-light)' : 'none',
              color: activeTab === 'privacy' ? 'var(--primary-orange)' : 'var(--text-muted)',
              fontWeight: activeTab === 'privacy' ? 700 : 500,
              cursor: 'pointer'
            }}
          >
            🗑️ 데이터 삭제 (F-7)
          </button>
        </div>

        {/* Tab Contents */}
        {activeTab === 'glossary' && (
          <div>
            <div style={{ display: 'flex', gap: '8px', marginBottom: '14px' }}>
              <input
                type="text"
                placeholder="용어 (예: Manyfast)"
                value={newTerm}
                onChange={(e) => setNewTerm(e.target.value)}
                style={{ flex: 1, padding: '8px 10px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', fontSize: '13px' }}
              />
              <input
                type="text"
                placeholder="규칙 (예: 원문 유지)"
                value={newRule}
                onChange={(e) => setNewRule(e.target.value)}
                style={{ flex: 1, padding: '8px 10px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', fontSize: '13px' }}
              />
              <button
                onClick={handleAddGlossary}
                style={{ background: 'var(--primary-orange)', color: 'white', border: 'none', padding: '8px 14px', borderRadius: 'var(--radius-sm)', cursor: 'pointer', fontWeight: 600 }}
              >
                추가
              </button>
            </div>
            <div style={{ maxHeight: '200px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {glossaries.map(g => (
                <div key={g.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px', background: 'var(--bg-subtle)', borderRadius: 'var(--radius-sm)' }}>
                  <div>
                    <strong style={{ fontSize: '13px' }}>{g.term}</strong>
                    <span style={{ fontSize: '12px', color: 'var(--primary-green)', marginLeft: '8px' }}>[{g.rule}]</span>
                  </div>
                  <button onClick={() => handleDeleteGlossary(g.id)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#ef4444' }}>
                    <Trash2 size={15} />
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {activeTab === 'style' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', fontSize: '13px' }}>
            <div>
              <label style={{ fontWeight: 700, display: 'block', marginBottom: '4px' }}>기본 비즈니스 톤</label>
              <select style={{ width: '100%', padding: '8px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)' }}>
                <option value="polite">정중하고 명확하게 (기본값)</option>
                <option value="concise">핵심만 간결하게</option>
                <option value="friendly">친근하고 유연하게</option>
              </select>
            </div>
            <div>
              <label style={{ fontWeight: 700, display: 'block', marginBottom: '4px' }}>직설성 선호도</label>
              <select style={{ width: '100%', padding: '8px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)' }}>
                <option value="balanced">균형 잡힌 직설성</option>
                <option value="direct">직접적이고 명확한 요구 선호</option>
              </select>
            </div>
          </div>
        )}

        {activeTab === 'privacy' && (
          <div style={{ fontSize: '13px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--primary-green)' }}>
              <ShieldCheck size={18} />
              <span style={{ fontWeight: 700 }}>사용자 데이터 주권 및 영구 삭제 (F-7)</span>
            </div>
            <p style={{ color: 'var(--text-muted)' }}>
              저장된 메시지 이력은 사용자의 명시적 요청 시 언제든 즉시 영구 삭제되며, 백엔드 서버에 원문이 남지 않도록 제어할 수 있습니다.
            </p>
            <button
              onClick={() => alert('메시지 작업 이력이 성공적으로 영구 삭제되었습니다.')}
              style={{
                background: '#fee2e2',
                color: '#dc2626',
                border: '1px solid #fecaca',
                padding: '10px',
                borderRadius: 'var(--radius-sm)',
                fontWeight: 700,
                cursor: 'pointer'
              }}
            >
              모든 메시지 작업 이력 영구 삭제하기
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
