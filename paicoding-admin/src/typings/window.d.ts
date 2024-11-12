// * global
declare global {
	interface Window {
		__REDUX_DEVTOOLS_EXTENSION_COMPOSE__: any;
	}
	interface Navigator {
		msSaveOrOpenBlob: (blob: Blob, fileName: string) => void;
	}
}
export {};
