import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const appName = process.argv[2];
if (!['web-user','pwa'].includes(appName)) throw new Error('Usage: node scripts/build-user.mjs web-user|pwa');
const source = path.join(repoRoot,'apps',appName);
const output = path.join(source,'dist');
await fs.rm(output,{recursive:true,force:true});
await fs.mkdir(output,{recursive:true});
for (const entry of await fs.readdir(source,{withFileTypes:true})) {
  if (entry.name==='dist' || entry.name==='node_modules' || entry.name==='tests' || entry.name==='package.json') continue;
  await fs.cp(path.join(source,entry.name),path.join(output,entry.name),{recursive:true});
}
await fs.cp(path.join(repoRoot,'packages','user-core'),path.join(output,'packages','user-core'),{recursive:true});
console.log(`Built ${appName} to ${path.relative(repoRoot,output)}`);
