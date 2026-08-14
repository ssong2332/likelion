import React from 'react';
import { Sparkles, Globe, ShieldAlert } from 'lucide-react';

export default function Editor({ text, setText, onAnalyze, loading, targetLang, setTargetLang }) {
  return (
    <div style={{
      background: 'white',
      border: '1px solid var(--border-color)',
      borderRadius: 'var(--radius-lg)',
      padding: '20px',
      boxShadow: 'var(--shadow-sm)',
      display: 'flex',
      flexDirection: 'column',
      height: '100%'
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 700 }}>1. 메시지 초안 작성</h2>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Globe size={15} color="var(--text-muted)" />
          <select 
            value={targetLang} 
            onChange={(e) => setTargetLang(e.target.value)}
            style={{
              padding: '6px 10px',
              borderRadius: 'var(--radius-sm)',
              border: '1px solid var(--border-color)',
              fontSize: '13px',
              background: 'var(--bg-subtle)'
            }}
          >
            <option value="en">영어 (English)</option>
            <option value="zh">중국어 (中文)</option>
            <option value="ja">일본어 (日本語)</option>
          </select>
        </div>
      </div>

      <textarea
        value={text}
        onChange={(e) => setText(e.target.value)}
        placeholder="업무 메시지 초안을 자유롭게 입력하세요. (예: 이거 리뷰 3일째 안 봐주셔서 오늘 배포 못 나갑니다. Manyfast 용어 확인해서 오늘 안에 피드백 주세요.)"
        style={{
          flex: 1,
          minHeight: '220px',
          width: '100%',
          padding: '14px',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-color)',
          fontSize: '14px',
          resize: 'vertical',
          outline: 'none',
          lineHeight: '1.6'
        }}
      />

      <div style={{ marginTop: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
          {text.length}자 입력됨
        </span>
        <button
          onClick={onAnalyze}
          disabled={loading || !text.trim()}
          style={{
            background: loading ? 'var(--text-subtle)' : 'var(--primary-orange)',
            color: 'white',
            border: 'none',
            padding: '10px 20px',
            borderRadius: 'var(--radius-md)',
            fontWeight: 700,
            fontSize: '14px',
            cursor: loading || !text.trim() ? 'not-allowed' : 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            boxShadow: 'var(--shadow-sm)',
            transition: 'background 0.2s'
          }}
        >
          <Sparkles size={16} />
          {loading ? 'AI 분석 및 교정 중...' : 'AI 분석 및 교정 실행'}
        </button>
      </div>
    </div>
  );
}
