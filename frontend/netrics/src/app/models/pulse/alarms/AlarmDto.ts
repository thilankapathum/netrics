export interface CellAlarmDto {
  alarmId: number,
  cellName: string,
  nodeName: string,
  severity: string,
  occurrenceTime: Date,
  alarmType: string,
  alarmCode: number,
  alarmName: string,
  location: string,
  ackState: string,
  clearState: string,
  specificProblem: string,
  additionalInfo: string,
  alarmSource: string,
}

export interface AlarmsDto {
  alarmId: number,
  nodeName: string,
  severity: string,
  occurrenceTime: Date,
  alarmType: string,
  alarmCode: number,
  alarmName: string,
  location: string,
  ackState: string,
  clearState: string,
  specificProblem: string,
  additionalInfo: string,
  alarmSource: string,
}

export interface AlarmFilter {
  period: string;
  nodeName?: string;
  severity?: string;
  alarmType?: string;
  alarmName?: string;
  ackState?: string;
  clearState?: string;
  alarmSource?: string;
  areaName?: string;
}

export interface AlarmTypeDto{
  name: string,
}

export interface AlarmSourceDto{
  name: string,
  label: string,
}
