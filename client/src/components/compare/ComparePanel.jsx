import React, { useState } from 'react';
import { Copy, Check, Info, AlertCircle, BookOpen, ThumbsUp } from 'lucide-react';

export default function ComparePanel({ result, originalText }) {
  const [copied, setCopied] = useState(false);
  const [editedText, setEditedText] = useState(result?.refinedText || '');

  React.useEffect(() => {
    if (result?.refinedText) {
      setEditedText(result.refinedText);
    }
  }, [result]);

  const handleCopy = () => {
    navigator.clipboard.writeText(editedText);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  if (!result) {
    return (
      <div style={{
        background: 'white',
        border: '1px dashed var(--border-color)',
        borderRadius: 'var(--radius-lg)',
        padding: '40px 20px',
        textAlign: 'center',
        color: 'var(--text-muted)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100%'
      }}>
        <Info size={32} color="var(--text-subtle)" style={{ marginBottom: '12px' }} />
        <h3 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-main)' }}>교정 결과 대기 중</h3>
        <p style={{ fontSize: '13px', marginTop: '6px' }}>왼쪽에서 메시지를 작성하고 'AI 분석 및 교정 실행'을 눌러주세요.</p>
      </div>
    );
  }

  return (
    <div style={{
      background: 'white',
      border: '1px solid var(--border-color)',
      borderRadius: 'var(--radius-lg)',
      padding: '20px',
      boxShadow: 'var(--shadow-sm)',
      display: 'flex',
      flexDirection: 'column',
      gap: '16px'
    }}>
      {/* Header & Copy Button */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 700 }}>2. AI 교정안 & 비교 검증</h2>
        <button
          onClick={handleCopy}
          style={{
            background: copied ? 'var(--primary-green)' : 'var(--bg-subtle)',
            color: copied ? 'white' : 'var(--text-main)',
            border: '1px solid var(--border-color)',
            padding: '6px 14px',
            borderRadius: 'var(--radius-sm)',
            fontSize: '13px',
            fontWeight: 600,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '6px'
          }}
        >
          {copied ? <Check size={14} /> : <Copy size={14} />}
          {copied ? '복사 완료 (Ctrl+V)' : '최종문 복사'}
        </button>
      </div>

      {/* Refined Text Editor */}
      <div>
        <label style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>
          최종 제안문 (직접 수정 가능)
        </label>
        <textarea
          value={editedText}
          onChange={(e) => setEditedText(e.target.value)}
          style={{
            width: '100%',
            minHeight: '90px',
            padding: '12px',
            borderRadius: 'var(--radius-md)',
            border: '1.5px solid var(--primary-orange)',
            fontSize: '14px',
            lineHeight: '1.5',
            outline: 'none',
            background: 'var(--primary-orange-light)'
          }}
        />
      </div>

      {/* Back Translation */}
      {result.backTranslation && (
        <div style={{
          background: 'var(--bg-subtle)',
          padding: '10px 14px',
          borderRadius: 'var(--radius-sm)',
          fontSize: '13px',
          borderLeft: '3px solid var(--primary-green)'
        }}>
          <span style={{ fontWeight: 700, color: 'var(--primary-green)', display: 'block', fontSize: '11px', marginBottom: '2px' }}>
            역번역 검증 (한국어 뜻)
          </span>
          {result.backTranslation}
        </div>
      )}

      {/* Extracted Business Facts */}
      {result.extractedInfo && (
        <div style={{ border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '12px' }}>
          <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)', display: 'block', marginBottom: '8px' }}>
            📌 보존된 핵심 업무 정보
          </span>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '6px', fontSize: '12px' }}>
            <div><strong>목적:</strong> {result.extractedInfo.purpose}</div>
            <div><strong>기한:</strong> {result.extractedInfo.deadline}</div>
            <div><strong>긴급도:</strong> <span style={{ color: 'var(--primary-orange)', fontWeight: 700 }}>{result.extractedInfo.urgency}</span></div>
            <div><strong>영향:</strong> {result.extractedInfo.businessImpact}</div>
          </div>
        </div>
      )}

      {/* Missing Info Warning */}
      {result.missingInfoWarnings && result.missingInfoWarnings.length > 0 && (
        <div style={{
          background: '#fef2f2',
          border: '1px solid #fee2e2',
          borderRadius: 'var(--radius-md)',
          padding: '10px 14px',
          fontSize: '12px',
          color: '#991b1b'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 700, marginBottom: '2px' }}>
            <AlertCircle size={14} color="#dc2626" />
            <span>핵심 업무 정보 누락 주의</span>
          </div>
          <p>{result.missingInfoWarnings[0].warning}</p>
          <p style={{ marginTop: '4px', color: '#b91c1c', fontStyle: 'italic' }}>
            💡 추천 보완: {result.missingInfoWarnings[0].suggestedCompletion}
          </p>
        </div>
      )}

      {/* Applied Glossary */}
      {result.appliedGlossary && result.appliedGlossary.length > 0 && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '12px', color: 'var(--text-muted)' }}>
          <BookOpen size={14} color="var(--primary-green)" />
          <span>적용된 용어집: <strong>{result.appliedGlossary.map(g => g.term).join(', ')}</strong> ({result.appliedGlossary[0].rule})</span>
        </div>
      )}
    </div>
  );
}
