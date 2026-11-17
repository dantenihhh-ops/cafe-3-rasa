import { Server as HttpServer } from 'http';
import { Server as SocketIOServer, Socket } from 'socket.io';

let io: SocketIOServer | null = null;

export const initSocket = (server: HttpServer, corsOrigin: string = '*'): SocketIOServer => {
  io = new SocketIOServer(server, {
    cors: {
      origin: corsOrigin === '*' ? '*' : corsOrigin.split(','),
      methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'],
      credentials: true,
    },
  });

  io.on('connection', (socket: Socket) => {
    console.log(`[Socket.io] Client connected: ${socket.id}`);

    // Meja pelanggan join ke room khusus meja tersebut
    socket.on('join_table', (tableNumber: string) => {
      const room = `table_${tableNumber}`;
      socket.join(room);
      console.log(`[Socket.io] Socket ${socket.id} joined ${room}`);
    });

    // Kasir / Admin join ke room khusus kasir
    socket.on('join_cashier', () => {
      socket.join('cashier_room');
      console.log(`[Socket.io] Socket ${socket.id} joined cashier_room`);
    });

    socket.on('disconnect', () => {
      console.log(`[Socket.io] Client disconnected: ${socket.id}`);
    });
  });

  return io;
};

export const getIO = (): SocketIOServer => {
  if (!io) {
    throw new Error('Socket.io belum diinisialisasi! Panggil initSocket terlebih dahulu.');
  }
  return io;
};

// Helper broadcaster untuk controller
export const emitToCashier = (event: string, payload: unknown) => {
  if (io) {
    io.to('cashier_room').emit(event, payload);
  }
};

export const emitToTable = (tableNumber: string, event: string, payload: unknown) => {
  if (io) {
    io.to(`table_${tableNumber}`).emit(event, payload);
  }
};

export const emitToAll = (event: string, payload: unknown) => {
  if (io) {
    io.emit(event, payload);
  }
};
