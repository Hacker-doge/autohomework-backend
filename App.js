import { useState } from 'react';
import logo from './logo.svg';
import './App.css';

function App() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState('');

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file && file.type === 'application/pdf') {
      setSelectedFile(file);
      setMessage('');
    } else {
      alert('Please select a PDF file');
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setUploading(true);
    setMessage('');

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
      const response = await fetch('http://localhost:8080/api/upload', {
        method: 'POST',
        body: formData,
      });

      const data = await response.json();

      if (response.ok) {
        setMessage(`✅ ${data.message}`);
        setSelectedFile(null);
        document.getElementById('fileInput').value = '';
      } else {
        setMessage(`❌ ${data.error}`);
      }
    } catch (error) {
      setMessage(`❌ Upload failed: ${error.message}`);
    } finally {
      setUploading(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (file && file.type === 'application/pdf') {
      setSelectedFile(file);
      setMessage('');
    } else {
      alert('Please drop a PDF file');
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        
        <div 
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          style={{
            border: '2px dashed #61dafb',
            borderRadius: '10px',
            padding: '30px',
            margin: '20px 0',
            textAlign: 'center',
            cursor: 'pointer'
          }}
        >
          <p>Drop PDF files here or click to upload</p>
          <input
            id="fileInput"
            type="file"
            accept=".pdf"
            onChange={handleFileChange}
            style={{ display: 'none' }}
          />
          <button
            onClick={() => document.getElementById('fileInput').click()}
            style={{
              padding: '10px 20px',
              margin: '10px',
              cursor: 'pointer',
              background: '#61dafb',
              border: 'none',
              borderRadius: '5px',
              fontSize: '16px'
            }}
          >
            Choose PDF
          </button>
          {selectedFile && (
            <div>
              <p>Selected: {selectedFile.name}</p>
              <button
                onClick={handleUpload}
                disabled={uploading}
                style={{
                  padding: '10px 20px',
                  cursor: uploading ? 'not-allowed' : 'pointer',
                  background: uploading ? '#ccc' : '#4CAF50',
                  color: 'white',
                  border: 'none',
                  borderRadius: '5px',
                  fontSize: '16px'
                }}
              >
                {uploading ? 'Uploading...' : 'Upload'}
              </button>
            </div>
          )}
          {message && (
            <p style={{ marginTop: '20px', fontSize: '14px' }}>{message}</p>
          )}
        </div>
      </header>
    </div>
  );
}

export default App;