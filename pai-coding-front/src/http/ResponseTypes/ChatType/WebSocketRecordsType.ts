export interface WebSocketRecordsType {
  answer?: string;
  answerTime?: string;
  answerType?: string;
  chatUid?: string;
  question?: string;
  questionTime?: string;

  msgType: 'chat' | 'question' | 'answer' | 'loading' | 'history';
}
