import * as L from 'leaflet';

declare module 'leaflet' {
  namespace VectorGrid {
    interface ProtobufOptions extends L.GridLayerOptions {
      vectorTileLayerStyles?: any;
      interactive?: boolean;
      maxNativeZoom?: number;
      vectorTileLayerNames?: string[];
    }

    function protobuf(
      url: string,
      options?: ProtobufOptions
    ): L.Layer;
  }
}
