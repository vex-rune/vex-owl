import React from 'react';
import styled from 'styled-components';
import { AppColors } from '../../../core/theme/appColors';
import { LoadingContainer, ErrorContainer, Button } from './styles';

interface DataTableViewProps {
  columns: string[];
  rows: React.ReactNode[][];
  isLoading?: boolean;
  error?: string | null;
  onRetry?: () => void;
  emptyText?: string;
}

const Table = styled.table`
  width: 100%;
  border-collapse: collapse;
  background-color: ${AppColors.cardBg};
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid ${AppColors.border};

  th {
    background-color: ${AppColors.bgTertiary};
    padding: 12px 24px;
    text-align: left;
    font-weight: 600;
    color: ${AppColors.textPrimary};
  }

  td {
    padding: 12px 24px;
    border-top: 1px solid ${AppColors.border};
    color: ${AppColors.textSecondary};
  }

  tr:hover td {
    background-color: ${AppColors.bgSecondary};
  }
`;

const EmptyRow = styled.tr`
  td {
    text-align: center;
    padding: 48px;
    color: ${AppColors.textTertiary};
  }
`;

export const DataTableView: React.FC<DataTableViewProps> = ({
  columns,
  rows,
  isLoading,
  error,
  onRetry,
  emptyText = '暂无数据',
}) => {
  if (isLoading) {
    return (
      <LoadingContainer>
        <div>加载中...</div>
      </LoadingContainer>
    );
  }

  if (error) {
    return (
      <ErrorContainer>
        <div>加载失败: {error}</div>
        {onRetry && (
          <Button onClick={onRetry} style={{ marginTop: 16 }}>
            重试
          </Button>
        )}
      </ErrorContainer>
    );
  }

  return (
    <Table>
      <thead>
        <tr>
          {columns.map((col, index) => (
            <th key={index}>{col}</th>
          ))}
        </tr>
      </thead>
      <tbody>
        {rows.length === 0 ? (
          <EmptyRow>
            <td colSpan={columns.length}>{emptyText}</td>
          </EmptyRow>
        ) : (
          rows.map((row, rowIndex) => (
            <tr key={rowIndex}>
              {row.map((cell, cellIndex) => (
                <td key={cellIndex}>{cell}</td>
              ))}
            </tr>
          ))
        )}
      </tbody>
    </Table>
  );
};