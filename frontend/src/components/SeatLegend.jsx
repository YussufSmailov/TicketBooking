function SeatLegend() {
  return (
    <div className="legend">
      <span><i className="seat-square legend-square"></i>Свободно</span>
      <span><i className="seat-square seat-square-selected legend-square"></i>Выбрано</span>
      <span><i className="seat-square seat-square-taken legend-square"></i>Занято</span>
    </div>
  );
}

export default SeatLegend;
