import React, { useState } from 'react';
import { MessageSquare, Send, Check, Copy } from 'lucide-react';
import { generateReplyDrafts } from '../../api/client';

export default function ReplyPanel() {
  const [receivedText, setReceivedText] = useState('');
  const [loading, setLoading] = useState(false);
  const [replyData, setReplyData] = useState(null);
  const [copiedIndex, setCopiedIndex] = useState(null);

  const handleGenerate = async () => {
    if (!receivedText.trim()) return;
    setLoading(true);
    const data = await generateReplyDrafts({ receivedMessage: receivedText });
    setReplyData(data);
    setLoading(false);
  };

  const handleCopy = (text, idx) => {
    navigator.clipboard.writeText(text);
    setCopiedIndex(idx);
    setTimeout(() => setCopiedIndex(null), 2000);
  };

  return (
    <div style={{
      background: 'white',
      border: '1px solid var(--border-color)',
      borderRadius: 'var(--radius-lg)',
      padding: '20px',
      boxShadow: 'var(--shadow-sm)',
      marginTop: '24px'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
        <MessageSquare size={18} color="var(--primary-orange)" />
        <h2 style={{ fontSize: '16px', fontWeight: 700 }}>3. 수신 메시지 회신 초안 생성 (F-8)</h2>
      </div>

      <div style={{ display: 'flex', gap: '10px', marginBottom: '16px' }}>
        <input
          type="text"
          value={receivedText}
          onChange={(e) => setReceivedText(e.target.value)}
          placeholder="상대방이 보낸 영어 메시지를 붙여넣으세요 (예: I have a few minor comments on your architecture draft...)"
          style={{
            flex: 1,
            padding: '10px 14px',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-color)',
            fontSize: '13px',
            outline: 'none'
          }}
        />
        <button
          onClick={handleGenerate}
          disabled={loading || !receivedText.trim()}
          style={{
            background: 'var(--primary-orange)',
            color: 'white',
            border: 'none',
            padding: '10px 18px',
            borderRadius: 'var(--radius-md)',
            fontWeight: 700,
            fontSize: '13px',
            cursor: 'pointer',
            whiteSpace: 'nowrap'
          }}
        >
          {loading ? '생성 중...' : '회신 템플릿 생성'}
        </button>
      </div>

      {replyData && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{
            background: 'var(--bg-subtle)',
            padding: '10px 14px',
            borderRadius: 'var(--radius-sm)',
            fontSize: '12px'
          }}>
            <strong>요청 분석:</strong> {replyData.analyzedRequest?.summary} (긴급도: {replyData.analyzedRequest?.urgency})
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '12px' }}>
            {replyData.suggestedReplies?.map((item, idx) => (
              <div key={idx} style={{
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-md)',
                padding: '14px',
                background: 'var(--bg-card)',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between'
              }}>
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                    <span style={{
                      fontSize: '11px',
                      fontWeight: 700,
                      padding: '2px 8px',
                      borderRadius: '4px',
                      background: 'var(--primary-green-light)',
                      color: 'var(--primary-green)'
                    }}>
                      {item.title}
                    </span>
                    <button
                      onClick={() => handleCopy(item.draftText, idx)}
                      style={{
                        background: 'none',
                        border: 'none',
                        cursor: 'pointer',
                        color: 'var(--text-muted)',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px',
                        fontSize: '12px'
                      }}
                    >
                      {copiedIndex === idx ? <Check size={14} color="var(--primary-green)" /> : <Copy size={14} />}
                      {copiedIndex === idx ? '복사됨' : '복사'}
                    </button>
                  </div>
                  <p style={{ fontSize: '13px', lineHeight: '1.5', color: 'var(--text-main)' }}>
                    "{item.draftText}"
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
