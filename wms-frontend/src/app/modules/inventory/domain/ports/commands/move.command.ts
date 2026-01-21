export interface MoveCommand {
  lpn: string;
  targetLocationCode: string;
  reason?: string;
}