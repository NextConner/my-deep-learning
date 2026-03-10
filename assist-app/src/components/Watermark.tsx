import React, { useEffect, useState } from 'react';

interface WatermarkProps {
  text: string;
}

export const Watermark: React.FC<WatermarkProps> = ({ text }) => {
  const [watermarkUrl, setWatermarkUrl] = useState<string>('');

  useEffect(() => {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const width = 300;
    const height = 200;
    canvas.width = width;
    canvas.height = height;

    // Clear background
    ctx.clearRect(0, 0, width, height);

    // Set font and style
    ctx.font = '14px "Inter", sans-serif';
    ctx.fillStyle = 'rgba(150, 150, 150, 0.15)'; // Very light gray
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    // Rotate for better coverage
    ctx.translate(width / 2, height / 2);
    ctx.rotate((-20 * Math.PI) / 180);

    // Draw text (username + date)
    const date = new Date().toLocaleDateString();
    ctx.fillText(text, 0, -10);
    ctx.fillText(date, 0, 10);

    setWatermarkUrl(canvas.toDataURL('image/png'));
  }, [text]);

  if (!watermarkUrl) return null;

  return (
    <div
      id="app-watermark"
      className="fixed inset-0 pointer-events-none z-[9999]"
      style={{
        backgroundImage: `url(${watermarkUrl})`,
        backgroundRepeat: 'repeat',
        opacity: 0.6,
      }}
    />
  );
};
