import { DatePicker } from "antd";
import type { Moment } from "moment";
import momentGenerateConfig from "rc-picker/lib/generate/moment";

const MyDatePicker = DatePicker.generatePicker<Moment>(momentGenerateConfig);

export default MyDatePicker;
