export const ElTagEnum = ["info", "danger", "success", "warning", "primary"]

export const getRandomElTagType = () => {
  return ElTagEnum[Math.floor(Math.random() * ElTagEnum.length)]
}
