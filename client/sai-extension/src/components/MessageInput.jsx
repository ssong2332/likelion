// src/components/MessageInput.jsx

function MessageInput({ value, onChange, onSubmit, isLoading }) {
  return (
    <div>
      <textarea
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="원문 메시지를 입력하세요"
        rows={4}
        style={{ width: '100%' }}
      />
      <button
        onClick={onSubmit}
        disabled={isLoading}
        style={{ color: '#000000' }}
      >
        {isLoading ? '교정 중...' : '교정하기'}
      </button>
    </div>
  )
}

export default MessageInput