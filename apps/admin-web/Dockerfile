FROM node:22-alpine
WORKDIR /app
COPY --chown=node:node package.json ./
COPY --chown=node:node server.mjs ./
COPY --chown=node:node public ./public
COPY --chown=node:node data/seed.json ./data/seed.json
RUN mkdir -p /var/lib/fastlove/data && chown -R node:node /var/lib/fastlove
USER node
ENV NODE_ENV=production
ENV DATA_DIR=/var/lib/fastlove/data
EXPOSE 4177
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 CMD node -e "fetch('http://127.0.0.1:4177/api/health').then(r=>process.exit(r.ok?0:1)).catch(()=>process.exit(1))"
CMD ["node", "server.mjs"]
