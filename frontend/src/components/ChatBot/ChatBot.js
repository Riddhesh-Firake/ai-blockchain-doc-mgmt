import React, { useState, useEffect, useRef } from 'react';
import ApiService from '../../services/ApiService';
import './ChatBot.css';

function ChatBot() {
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [chatMessages, setChatMessages] = useState([
    {
      id: 1,
      type: 'bot',
      content: 'Hello! I\'m your AI consultation assistant. How can I help you today?',
      timestamp: new Date()
    }
  ]);
  const [chatInput, setChatInput] = useState('');
  const [isChatLoading, setIsChatLoading] = useState(false);
  const chatMessagesRef = useRef(null);
  const chatInputRef = useRef(null);

  useEffect(() => {
    if (chatMessagesRef.current) {
      chatMessagesRef.current.scrollTop = chatMessagesRef.current.scrollHeight;
    }
  }, [chatMessages]);

  const handleChatSubmit = async (e) => {
    e.preventDefault();
    
    if (!chatInput.trim()) return;

    const userMessage = {
      id: Date.now(),
      type: 'user',
      content: chatInput,
      timestamp: new Date()
    };

    setChatMessages(prev => [...prev, userMessage]);
    setChatInput('');
    setIsChatLoading(true);

    try {
      // Default to finance domain for general chat queries
      const response = await ApiService.submitConsultation({
        domain: 'finance',
        query: chatInput
      });

      const botMessage = {
        id: Date.now() + 1,
        type: 'bot',
        content: response.response,
        timestamp: new Date()
      };

      setChatMessages(prev => [...prev, botMessage]);
    } catch (error) {
      const errorMessage = {
        id: Date.now() + 1,
        type: 'bot',
        content: 'I apologize, but I\'m having trouble processing your request right now. Please try again later.',
        timestamp: new Date()
      };

      setChatMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsChatLoading(false);
    }
  };

  const toggleChat = () => {
    setIsChatOpen(!isChatOpen);
    if (!isChatOpen) {
      setTimeout(() => {
        chatInputRef.current?.focus();
      }, 100);
    }
  };

  const formatResponseText = (text) => {
    if (!text) return text;
    
    // Convert markdown-style formatting to proper line breaks and formatting
    return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') // Bold text
      .replace(/\*(.*?)\*/g, '<em>$1</em>') // Italic text
      .replace(/(\d+\.\s)/g, '\n$1') // Add line breaks before numbered lists
      .replace(/(\*\s)/g, '\n• ') // Convert asterisk lists to bullet points
      .trim();
  };

  const formatTime = (date) => {
    return new Date(date).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className={`chat-bubble ${isChatOpen ? 'open' : ''}`}>
      {!isChatOpen ? (
        <button className="chat-toggle" onClick={toggleChat}>
          <span className="chat-icon">💬</span>
          <span className="chat-label">Ask AI</span>
        </button>
      ) : (
        <div className="chat-window">
          <div className="chat-header">
            <div className="chat-header-content">
              <span className="chat-title">AI Consultant</span>
              <span className="chat-status">Online</span>
            </div>
            <button className="chat-close" onClick={toggleChat}>
              ✕
            </button>
          </div>
          
          <div className="chat-messages" ref={chatMessagesRef}>
            {chatMessages.map(message => (
              <div key={message.id} className={`message ${message.type}`}>
                <div 
                  className="message-content"
                  dangerouslySetInnerHTML={{ 
                    __html: message.type === 'bot' ? formatResponseText(message.content) : message.content 
                  }}
                />
                <div className="message-time">
                  {formatTime(message.timestamp)}
                </div>
              </div>
            ))}
            {isChatLoading && (
              <div className="message bot">
                <div className="message-content">
                  <div className="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                </div>
              </div>
            )}
          </div>

          <form onSubmit={handleChatSubmit} className="chat-input-form">
            <input
              ref={chatInputRef}
              type="text"
              value={chatInput}
              onChange={(e) => setChatInput(e.target.value)}
              placeholder="Type your message..."
              className="chat-input"
              disabled={isChatLoading}
            />
            <button
              type="submit"
              className="chat-send"
              disabled={!chatInput.trim() || isChatLoading}
            >
              ➤
            </button>
          </form>
        </div>
      )}
    </div>
  );
}

export default ChatBot;