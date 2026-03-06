'use client';

import { useEffect } from 'react';
import { MapContainer, TileLayer, CircleMarker, Tooltip } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

const stateMarkers: {
  name: string;
  region: string;
  lat: number;
  lon: number;
  color: string;
  radius: number;
}[] = [
  // North – blue
  { name: 'Uttar Pradesh', region: 'North', lat: 26.8, lon: 80.9, color: '#2563EB', radius: 38 },
  { name: 'Bihar',         region: 'North', lat: 25.1, lon: 85.3, color: '#2563EB', radius: 26 },
  // Central & West – orange
  { name: 'Madhya Pradesh', region: 'Central & West', lat: 23.5, lon: 77.6, color: '#F97316', radius: 36 },
  { name: 'Maharashtra',    region: 'Central & West', lat: 19.7, lon: 75.7, color: '#F97316', radius: 34 },
  { name: 'Gujarat',        region: 'Central & West', lat: 22.3, lon: 71.2, color: '#F97316', radius: 28 },
  // South – green
  { name: 'Telangana',       region: 'South', lat: 17.4, lon: 79.1, color: '#16A34A', radius: 24 },
  { name: 'Andhra Pradesh',  region: 'South', lat: 15.9, lon: 79.7, color: '#16A34A', radius: 30 },
  { name: 'Karnataka',       region: 'South', lat: 15.3, lon: 75.7, color: '#16A34A', radius: 30 },
];

export default function IndiaMap() {
  // Leaflet reads window at module load; this component is only rendered client-side
  useEffect(() => {}, []);

  return (
    <MapContainer
      center={[22, 80]}
      zoom={5}
      scrollWheelZoom={false}
      style={{ height: '460px', width: '100%', borderRadius: '12px', zIndex: 0 }}
    >
      {/* CartoDB Positron – clean, light, professional, free/no API key */}
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
        url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png"
      />

      {/* State-level coverage blobs */}
      {stateMarkers.map((s) => (
        <CircleMarker
          key={s.name}
          center={[s.lat, s.lon]}
          radius={s.radius}
          pathOptions={{
            fillColor: s.color,
            color: s.color,
            fillOpacity: 0.45,
            weight: 1.5,
          }}
        >
          <Tooltip direction="top" offset={[0, -s.radius]} opacity={0.95}>
            <span style={{ fontWeight: 700 }}>{s.name}</span>
            <br />
            <span style={{ color: '#666', fontSize: '11px' }}>{s.region}</span>
          </Tooltip>
        </CircleMarker>
      ))}

      {/* Hyderabad HQ pin */}
      <CircleMarker
        center={[17.38, 78.49]}
        radius={9}
        pathOptions={{
          fillColor: '#EF4444',
          color: '#ffffff',
          fillOpacity: 1,
          weight: 2.5,
        }}
      >
        <Tooltip direction="top" offset={[0, -12]} opacity={1} permanent>
          <span style={{ fontWeight: 700, color: '#DC2626', fontSize: '12px' }}>
            📍 Hyderabad HQ
          </span>
        </Tooltip>
      </CircleMarker>
    </MapContainer>
  );
}
