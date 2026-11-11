import ZoneMap from "./ZoneMap";

const centerX = 300;
const centerY = 215;

function pointOnEllipse(radiusX, radiusY, angle) {
  const radians = (angle * Math.PI) / 180;
  return {
    x: centerX + radiusX * Math.cos(radians),
    y: centerY + radiusY * Math.sin(radians)
  };
}

function standPath(inner, outer, startAngle, endAngle) {
  const outerStart = pointOnEllipse(outer.rx, outer.ry, startAngle);
  const outerEnd = pointOnEllipse(outer.rx, outer.ry, endAngle);
  const innerEnd = pointOnEllipse(inner.rx, inner.ry, endAngle);
  const innerStart = pointOnEllipse(inner.rx, inner.ry, startAngle);
  return [
    `M ${outerStart.x} ${outerStart.y}`,
    `A ${outer.rx} ${outer.ry} 0 0 1 ${outerEnd.x} ${outerEnd.y}`,
    `L ${innerEnd.x} ${innerEnd.y}`,
    `A ${inner.rx} ${inner.ry} 0 0 0 ${innerStart.x} ${innerStart.y}`,
    "Z"
  ].join(" ");
}

function makeStand(name, inner, outer, startAngle, endAngle, labelRotation) {
  const middleAngle = (startAngle + endAngle) / 2;
  const label = pointOnEllipse((inner.rx + outer.rx) / 2, (inner.ry + outer.ry) / 2, middleAngle);
  return {
    name: name,
    path: standPath(inner, outer, startAngle, endAngle),
    labelX: label.x,
    labelY: label.y + 5,
    labelRotation: labelRotation
  };
}

const innerRing = { rx: 212, ry: 142 };
const outerRing = { rx: 290, ry: 202 };
const vipInner = { rx: 176, ry: 116 };
const vipOuter = { rx: 206, ry: 137 };

const stadiumShapes = [
  makeStand("Северная", innerRing, outerRing, -133, -47, 0),
  makeStand("Восточная", innerRing, outerRing, -43, 43, 90),
  makeStand("Южная", innerRing, outerRing, 47, 133, 0),
  makeStand("Западная", innerRing, outerRing, 137, 223, -90),
  makeStand("VIP", vipInner, vipOuter, 157, 203, -90)
];

const field = (
  <g aria-hidden="true">
    <rect x="160" y="128" width="280" height="174" rx="6" fill="#3f9b4f" />
    <rect x="166" y="134" width="268" height="162" fill="none" stroke="#e8f5e9" strokeWidth="2" />
    <line x1="300" y1="134" x2="300" y2="296" stroke="#e8f5e9" strokeWidth="2" />
    <circle cx="300" cy="215" r="26" fill="none" stroke="#e8f5e9" strokeWidth="2" />
    <rect x="166" y="180" width="34" height="70" fill="none" stroke="#e8f5e9" strokeWidth="2" />
    <rect x="400" y="180" width="34" height="70" fill="none" stroke="#e8f5e9" strokeWidth="2" />
  </g>
);

function StadiumMap({ seats, selectedSeat, onSelect }) {
  return (
    <ZoneMap
      viewBox="0 0 600 430"
      shapes={stadiumShapes}
      background={field}
      seats={seats}
      selectedSeat={selectedSeat}
      onSelect={onSelect}
      zoneWord="Сектор"
    />
  );
}

export default StadiumMap;
