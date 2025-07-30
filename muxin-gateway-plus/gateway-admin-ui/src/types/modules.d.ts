declare module 'path-browserify' {
  const path: {
    resolve(...paths: string[]): string
    join(...paths: string[]): string
    dirname(path: string): string
    basename(path: string, ext?: string): string
    extname(path: string): string
    relative(from: string, to: string): string
    parse(path: string): {
      root: string
      dir: string
      base: string
      ext: string
      name: string
    }
  }
  export default path
} 